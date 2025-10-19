package service

import (
	"fmt"
	"mosprom/api/internal/model"
	"mosprom/api/internal/repository"
)

type UserService struct{}

func NewUserService() *UserService { return &UserService{} }

func (s *UserService) GetAllUsers() ([]model.User, error) {
	return repository.GetAllUsers()
}

type CreateUserInput struct {
	TelegramName string
	Name         string
	Password     string
	Description  string
	PhotoPath    string // относительный путь к файлу
	Achievements []string
	EventsCount  int
	University   string
	Technologies []string // имена
	Directions   []string // имена
}

type UpdateUserInput struct {
	ID           uint
	TelegramName *string
	Name         *string
	Password     *string
	Description  *string
	PhotoPath    *string
	Achievements *[]string
	EventsCount  *int
	University   *string
	Technologies *[]string
	Directions   *[]string
}

func (s *UserService) CreateUser(input CreateUserInput) (model.User, error) {
	techs, err := repository.FindOrCreateTechnologiesByNames(input.Technologies)
	if err != nil {
		return model.User{}, err
	}
	dirs, err := repository.FindOrCreateDirectionsByNames(input.Directions)
	if err != nil {
		return model.User{}, err
	}

	achievements := input.Achievements
	if achievements == nil {
		achievements = []string{}
	}
	achievements = append(achievements, "🎉 Joined the community! First step to becoming a tech superstar!")

	user := model.User{
		TelegramName: input.TelegramName,
		Name:         input.Name,
		Password:     input.Password,
		Description:  input.Description,
		Photo:        input.PhotoPath,
		Achievements: achievements,
		EventsCount:  input.EventsCount,
		University:   input.University,
		Technologies: techs,
		Directions:   dirs,
	}
	if err := repository.CreateUser(&user); err != nil {
		return model.User{}, err
	}
	return user, nil
}

func (s *UserService) UpdateUser(input UpdateUserInput) (model.User, error) {
	user, err := repository.GetUserByID(input.ID)
	if err != nil {
		return model.User{}, err
	}
	if input.TelegramName != nil {
		user.TelegramName = *input.TelegramName
	}
	if input.Name != nil {
		user.Name = *input.Name
	}
	if input.Password != nil {
		user.Password = *input.Password
	}
	if input.Description != nil {
		user.Description = *input.Description
	}
	if input.PhotoPath != nil {
		user.Photo = *input.PhotoPath
	}
	if input.Achievements != nil {
		user.Achievements = *input.Achievements
	}
	if input.EventsCount != nil {
		user.EventsCount = *input.EventsCount
	}
	if input.University != nil {
		user.University = *input.University
	}

	if input.Technologies != nil || input.Directions != nil {
		var techs []model.Technology
		var dirs []model.Direction
		if input.Technologies != nil {
			if ts, err := repository.FindOrCreateTechnologiesByNames(*input.Technologies); err == nil {
				techs = ts
			} else {
				return model.User{}, err
			}
		}
		if input.Directions != nil {
			if ds, err := repository.FindOrCreateDirectionsByNames(*input.Directions); err == nil {
				dirs = ds
			} else {
				return model.User{}, err
			}
		}
		if err := repository.ReplaceUserAssociations(&user, techs, dirs); err != nil {
			return model.User{}, err
		}
	}

	if err := repository.UpdateUser(&user); err != nil {
		return model.User{}, err
	}
	return user, nil
}

func (s *UserService) GetUserByID(id uint) (model.User, error) {
	return repository.GetUserByID(id)
}

func (s *UserService) DeleteUser(id uint) error {
	return repository.DeleteUser(id)
}

func (s *UserService) AuthenticateByTelegram(tg, password string) (model.User, error) {
	user, err := repository.GetUserByTelegram(tg)
	if err != nil {
		return model.User{}, err
	}
	if user.Password != password { // NOTE: replace with bcrypt in production
		return model.User{}, fmt.Errorf("invalid credentials")
	}
	return user, nil
}

func (s *UserService) TechnologiesByUserID(userID uint) ([]model.Technology, error) {
	return repository.GetTechnologiesByUserID(userID)
}

func (s *UserService) SetUserTechnologies(userID uint, names []string) ([]model.Technology, error) {
	techs, err := repository.FindOrCreateTechnologiesByNames(names)
	if err != nil {
		return nil, err
	}
	if err := repository.ReplaceUserTechnologies(userID, techs); err != nil {
		return nil, err
	}
	return techs, nil
}
func (s *UserService) GetUserAchievements(userID uint) ([]string, error) {
	return repository.GetUserAchievements(userID)
}
func (s *UserService) AddAchievementToUser(userID uint, achievement string) error {
	return repository.AddAchievementToUser(userID, achievement)
}
