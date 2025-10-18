package service

import (
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/repository"
)

type DirectionService struct{}

func NewDirectionService() *DirectionService { return &DirectionService{} }

func (s *DirectionService) List() ([]model.Direction, error) {
	return repository.ListDirections()
}
