package service

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/repository"
)

type ClubService struct{}

func NewClubService() *ClubService { return &ClubService{} }

type CreateClubInput struct {
	Name           string
	Description    string
	DirectionNames []string
}

func (s *ClubService) CreateClub(input CreateClubInput, creatorID uint) (model.Club, error) {
	// Find or create directions by names and attach to club
	dirs, err := repository.FindOrCreateDirectionsByNames(input.DirectionNames)
	if err != nil {
		return model.Club{}, err
	}
	club := model.Club{
		Name:        input.Name,
		Description: input.Description,
		CreatorID:   creatorID,
		Directions:  dirs,
	}
	if err := repository.CreateClub(&club); err != nil {
		return model.Club{}, err
	}
	// set ClubID on directions if needed
	if len(dirs) > 0 {
		if err := db.DB.Model(&model.Direction{}).Where("id IN ?", idsOfDirections(dirs)).Update("club_id", club.ID).Error; err != nil {
			return model.Club{}, err
		}
	}
	return club, nil
}

func idsOfDirections(ds []model.Direction) []uint {
	ids := make([]uint, 0, len(ds))
	for _, d := range ds {
		ids = append(ids, d.ID)
	}
	return ids
}

func (s *ClubService) GetByName(name string) (model.Club, error) {
	return repository.GetClubByName(name)
}

func (s *ClubService) ListByDirections(names []string) ([]model.Club, error) {
	return repository.ListClubsByDirections(names)
}

func (s *ClubService) SetLogo(clubID uint, filename string) error {
	return repository.SetClubLogo(clubID, filename)
}
