package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
)

func GetAllUsers() ([]model.User, error) {
	var users []model.User
	err := db.DB.Find(&users).Error
	return users, err
}

func CreateUser(name, email, password, role string) (model.User, error) {
	user := model.User{Name: name, Email: email, Password: password, Role: role}
	err := db.DB.Create(&user).Error
	return user, err
}

func GetUserByEmail(email string) (model.User, error) {
	var user model.User
	err := db.DB.Where("email = ?", email).First(&user).Error
	return user, err
}
