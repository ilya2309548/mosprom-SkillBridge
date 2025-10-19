package service

import (
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/repository"
)

type LikeService struct{}

func NewLikeService() *LikeService {
	return &LikeService{}
}

func (s *LikeService) LikePost(userID, postID uint) error {
	_, err := repository.GetLikeByUserAndPost(userID, postID)
	if err == nil {
		return nil
	}

	like := model.Like{
		UserID: userID,
		PostID: postID,
	}
	return repository.CreateLike(&like)
}

func (s *LikeService) UnlikePost(userID, postID uint) error {
	return repository.DeleteLike(userID, postID)
}

func (s *LikeService) GetLikesByPostID(postID uint) ([]model.Like, error) {
	return repository.GetLikesByPostID(postID)
}

func (s *LikeService) GetLikesCountByPostID(postID uint) (int64, error) {
	return repository.GetLikesCountByPostID(postID)
}

func (s *LikeService) HasUserLikedPost(userID, postID uint) (bool, error) {
	_, err := repository.GetLikeByUserAndPost(userID, postID)
	if err != nil {
		return false, nil
	}
	return true, nil
}
