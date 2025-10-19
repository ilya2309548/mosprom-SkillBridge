package repository

import (
	"mosprom/api/internal/db"
	"mosprom/api/internal/model"
)

func ListDirections() ([]model.Direction, error) {
	var dirs []model.Direction
	// Preload technologies for convenience
	err := db.DB.Preload("Technologies").Find(&dirs).Error
	return dirs, err
}

func GetTechnologiesByDirectionID(directionID uint) ([]model.Technology, error) {
	var techs []model.Technology
	err := db.DB.Model(&model.Technology{}).
		Joins("JOIN direction_technologies dt ON dt.technology_id = technologies.id").
		Where("dt.direction_id = ?", directionID).
		Find(&techs).Error
	return techs, err
}
