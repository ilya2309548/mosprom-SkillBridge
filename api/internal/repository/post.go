package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
)

func CreatePost(post *model.Post) error {
	return db.DB.Create(post).Error
}

func GetPostByID(id uint) (model.Post, error) {
	var post model.Post
	err := db.DB.Preload("Club").First(&post, id).Error
	return post, err
}

func GetPostsByClubID(clubID uint) ([]model.Post, error) {
	var posts []model.Post
	err := db.DB.Where("club_id = ?", clubID).Preload("Club").Find(&posts).Error
	return posts, err
}

func GetAllPosts() ([]model.Post, error) {
	var posts []model.Post
	err := db.DB.Preload("Club").Find(&posts).Error
	return posts, err
}

func UpdatePost(post *model.Post) error {
	return db.DB.Save(post).Error
}

func DeletePost(id uint) error {
	return db.DB.Delete(&model.Post{}, id).Error
}
