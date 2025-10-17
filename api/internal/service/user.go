package service

import (
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/repository"
	"errors"
)

type UserService struct{}

func NewUserService() *UserService {
	return &UserService{}
}

func (s *UserService) GetAllUsers() ([]model.User, error) {
	return repository.GetAllUsers()
}

func (s *UserService) CreateUser(name, email, password, role string) (model.User, error) {
	return repository.CreateUser(name, email, password, role)
}

func (s *UserService) AuthenticateUser(email, password string) (model.User, error) {
	user, err := repository.GetUserByEmail(email)
	if err != nil {
		return model.User{}, err
	}
	// Пока без bcrypt: простое сравнение (ВНИМАНИЕ: небезопасно для продакшена)
	if user.Password != password {
		return model.User{}, errors.New("invalid credentials")
	}
	return user, nil
}
