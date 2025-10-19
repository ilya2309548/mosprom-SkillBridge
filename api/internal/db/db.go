package db

import (
	"2gis-calm-map/api/config"
	"2gis-calm-map/api/internal/model"
	"log"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

var DB *gorm.DB

func Init(cfg *config.Config) {
	database, err := gorm.Open(postgres.Open(cfg.GetDSN()), &gorm.Config{})
	if err != nil {
		log.Fatal("failed to connect database: ", err)
	}
	DB = database

	log.Println("Database connected")

	// MIGRATION: порядок важен из-за внешних ключей
	// 1) Club (родитель)
	// 2) Direction (имеет FK на Club)
	// 3) Technology (имеет FK на Direction)
	// 4) User (создаёт join-таблицы для many2many c Technology/Direction)
	if err := DB.AutoMigrate(
		&model.Club{},
		&model.Direction{},
		&model.Technology{},
		&model.User{},
		&model.Post{},
	); err != nil {
		log.Fatal("failed to migrate database: ", err)
	}

	// Backfill: if the old one-to-many column technologies.direction_id exists, populate the new join table
	migrator := DB.Migrator()
	if migrator.HasColumn(&model.Technology{}, "direction_id") {
		// Ensure unique index on the join to avoid duplicates
		_ = DB.Exec("CREATE UNIQUE INDEX IF NOT EXISTS ux_direction_technologies ON direction_technologies (direction_id, technology_id)").Error
		// Insert pairs for existing rows
		_ = DB.Exec(`
			INSERT INTO direction_technologies (direction_id, technology_id)
			SELECT direction_id, id FROM technologies WHERE direction_id IS NOT NULL
			ON CONFLICT (direction_id, technology_id) DO NOTHING
		`).Error
	}

	// Ensure unique index on post participants join table
	_ = DB.Exec("CREATE UNIQUE INDEX IF NOT EXISTS ux_post_participants ON post_participants (post_id, user_id)").Error
}
