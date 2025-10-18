package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
	"strings"
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
	err := db.DB.Joins("JOIN club_directions cd ON cd.club_id = clubs.id").
		Joins("JOIN directions d ON d.id = cd.direction_id").
		Where("d.name IN ?", directionNames).
		Preload("Directions").Preload("Events").Preload("Creator").
		Group("clubs.id").
		Find(&clubs).Error
	return clubs, err
}

func SetClubLogo(clubID uint, filename string) error {
	return db.DB.Model(&model.Club{}).Where("id = ?", clubID).Update("logo", filename).Error
}

// ListClubsFiltered returns clubs filtered by optional exact name and by having ALL of the given directions.
func ListClubsFiltered(name *string, directions []string) ([]model.Club, error) {
	var clubs []model.Club
	q := db.DB.Model(&model.Club{}).
		Preload("Directions").Preload("Events").Preload("Creator")

	if name != nil && strings.TrimSpace(*name) != "" {
		q = q.Where("clubs.name = ?", strings.TrimSpace(*name))
	}
	if len(directions) > 0 {
		dirs := uniqueStrings(directions)
		q = q.Joins("JOIN club_directions cd ON cd.club_id = clubs.id").
			Joins("JOIN directions d ON d.id = cd.direction_id").
			Where("d.name IN ?", dirs).
			Group("clubs.id").
			Having("COUNT(DISTINCT d.id) = ?", len(dirs))
	}
	if err := q.Find(&clubs).Error; err != nil {
		return nil, err
	}
	return clubs, nil
}

func uniqueStrings(in []string) []string {
	m := make(map[string]struct{}, len(in))
	out := make([]string, 0, len(in))
	for _, v := range in {
		v = strings.TrimSpace(v)
		if v == "" {
			continue
		}
		if _, ok := m[v]; ok {
			continue
		}
		m[v] = struct{}{}
		out = append(out, v)
	}
	return out
}
