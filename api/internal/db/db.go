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

	// MIGRATION: автоматически создаёт таблицы, если их нет
	if err := DB.AutoMigrate(&model.User{}); err != nil {
		log.Fatal("failed to migrate database: ", err)
	}

}
