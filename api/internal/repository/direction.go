package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
)

func ListDirections() ([]model.Direction, error) {
	var dirs []model.Direction
	// Preload technologies for convenience
	err := db.DB.Preload("Technologies").Find(&dirs).Error
	return dirs, err
}
