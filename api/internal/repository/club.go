package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
)

func CreateClub(club *model.Club) error {
	return db.DB.Create(club).Error
}

func GetClubByName(name string) (model.Club, error) {
	var club model.Club
	err := db.DB.Preload("Directions").Preload("Events").Preload("Creator").Where("name = ?", name).First(&club).Error
	return club, err
}

func ListClubsByDirections(directionNames []string) ([]model.Club, error) {
	var clubs []model.Club
	if len(directionNames) == 0 {
		// return all with preloads
		err := db.DB.Preload("Directions").Preload("Events").Preload("Creator").Find(&clubs).Error
		return clubs, err
	}
	// join directions to filter by names
	err := db.DB.Joins("JOIN directions on directions.club_id = clubs.id").
		Where("directions.name IN ?", directionNames).
		Preload("Directions").Preload("Events").Preload("Creator").
		Group("clubs.id").
		Find(&clubs).Error
	return clubs, err
}

func SetClubLogo(clubID uint, filename string) error {
	return db.DB.Model(&model.Club{}).Where("id = ?", clubID).Update("logo", filename).Error
}
