package service

import (
	"mosprom/api/internal/model"
	"mosprom/api/internal/repository"

	"github.com/google/uuid"
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

	chatID := uuid.New()

	club := model.Club{
		Name:        input.Name,
		Description: input.Description,
		CreatorID:   creatorID,
		Directions:  dirs,
		ChatID:      chatID.String(),
	}
	if err := repository.CreateClub(&club); err != nil {
		return model.Club{}, err
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

func (s *ClubService) ListFiltered(name *string, directions []string) ([]model.Club, error) {
	return repository.ListClubsFiltered(name, directions)
}

func (s *ClubService) Subscribe(userID, clubID uint) error {
	return repository.SubscribeUserToClub(userID, clubID)
}

func (s *ClubService) Subscribers(clubID uint) ([]model.User, error) {
	return repository.GetClubSubscribers(clubID)
}

func (s *ClubService) ClubsOfUser(userID uint) ([]model.Club, error) {
	return repository.GetUserClubs(userID)
}

func (s *ClubService) GetByChatID(chatID string) (model.Club, error) {
	return repository.GetClubByChatID(chatID)
}
