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
	// 2) Event (ссылается на Club)
	// 3) Direction (имеет FK на Club)
	// 4) Technology (имеет FK на Direction)
	// 5) User (создаёт join-таблицы для many2many c Technology/Direction)
	if err := DB.AutoMigrate(
		&model.Club{},
		&model.Event{},
		&model.Direction{},
		&model.Technology{},
		&model.User{},
	); err != nil {
		log.Fatal("failed to migrate database: ", err)
	}

}
