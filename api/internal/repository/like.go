package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"
)

func CreateLike(like *model.Like) error {
	return db.DB.Create(like).Error
}

func DeleteLike(userID, postID uint) error {
	return db.DB.Where("user_id = ? AND post_id = ?", userID, postID).Delete(&model.Like{}).Error
}

func GetLikeByUserAndPost(userID, postID uint) (model.Like, error) {
	var like model.Like
	err := db.DB.Where("user_id = ? AND post_id = ?", userID, postID).First(&like).Error
	return like, err
}

func GetLikesByPostID(postID uint) ([]model.Like, error) {
	var likes []model.Like
	err := db.DB.Where("post_id = ?", postID).Preload("User").Find(&likes).Error
	return likes, err
}

func GetLikesCountByPostID(postID uint) (int64, error) {
	var count int64
	err := db.DB.Model(&model.Like{}).Where("post_id = ?", postID).Count(&count).Error
	return count, err
}
