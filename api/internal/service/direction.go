package service

import (
	"mosprom/api/internal/model"
	"mosprom/api/internal/repository"
)

type DirectionService struct{}

func NewDirectionService() *DirectionService { return &DirectionService{} }

func (s *DirectionService) List() ([]model.Direction, error) {
	return repository.ListDirections()
}

func (s *DirectionService) TechnologiesByDirectionID(directionID uint) ([]model.Technology, error) {
	return repository.GetTechnologiesByDirectionID(directionID)
}
