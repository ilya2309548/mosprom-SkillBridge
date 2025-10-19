package service

import (
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/repository"
	"time"
)

type PostService struct{}

func NewPostService() *PostService {
	return &PostService{}
}

func (s *PostService) Join(userID, postID uint) error {
	return repository.JoinUserToPost(userID, postID)
}

type CreatePostInput struct {
	Title          string            `json:"title" binding:"required"`
	Description    string            `json:"description"`
	Type           model.PostType    `json:"type" binding:"required"`
	StartDate      *time.Time        `json:"start_date"`
	EndDate        *time.Time        `json:"end_date"`
	AgeRestriction *int              `json:"age_restriction"`
	Format         *model.PostFormat `json:"format"`
	Address        string            `json:"address"`
	ClubID         uint              `json:"club_id" binding:"required"`
}

type UpdatePostInput struct {
	ID             uint
	Title          *string           `json:"title"`
	Description    *string           `json:"description"`
	Type           *model.PostType   `json:"type"`
	StartDate      *time.Time        `json:"start_date"`
	EndDate        *time.Time        `json:"end_date"`
	AgeRestriction *int              `json:"age_restriction"`
	Format         *model.PostFormat `json:"format"`
	Address        *string           `json:"address"`
}

func (s *PostService) CreatePost(input CreatePostInput) (model.Post, error) {
	post := model.Post{
		Title:          input.Title,
		Description:    input.Description,
		Type:           input.Type,
		StartDate:      input.StartDate,
		EndDate:        input.EndDate,
		AgeRestriction: input.AgeRestriction,
		Format:         input.Format,
		Address:        input.Address,
		ClubID:         input.ClubID,
	}

	if err := repository.CreatePost(&post); err != nil {
		return model.Post{}, err
	}

	createdPost, err := repository.GetPostByID(post.ID)
	if err != nil {
		return model.Post{}, err
	}

	return createdPost, nil
}

func (s *PostService) GetPostByID(id uint) (model.Post, error) {
	return repository.GetPostByID(id)
}

func (s *PostService) GetPostsByClubID(clubID uint) ([]model.Post, error) {
	return repository.GetPostsByClubID(clubID)
}

func (s *PostService) GetAllPosts() ([]model.Post, error) {
	return repository.GetAllPosts()
}

func (s *PostService) UpdatePost(input UpdatePostInput) (model.Post, error) {
	post, err := repository.GetPostByID(input.ID)
	if err != nil {
		return model.Post{}, err
	}

	if input.Title != nil {
		post.Title = *input.Title
	}
	if input.Description != nil {
		post.Description = *input.Description
	}
	if input.Type != nil {
		post.Type = *input.Type
	}
	if input.StartDate != nil {
		post.StartDate = input.StartDate
	}
	if input.EndDate != nil {
		post.EndDate = input.EndDate
	}
	if input.AgeRestriction != nil {
		post.AgeRestriction = input.AgeRestriction
	}
	if input.Format != nil {
		post.Format = input.Format
	}
	if input.Address != nil {
		post.Address = *input.Address
	}

	if err := repository.UpdatePost(&post); err != nil {
		return model.Post{}, err
	}

	updatedPost, err := repository.GetPostByID(post.ID)
	if err != nil {
		return model.Post{}, err
	}

	return updatedPost, nil
}

func (s *PostService) DeletePost(id uint) error {
	return repository.DeletePost(id)
}

func (s *PostService) JoinedPosts(userID uint) ([]model.Post, error) {
	return repository.GetUserJoinedPosts(userID)
}

// SetPostTechnologies replaces technologies for a post, creating any missing by name
func (s *PostService) SetPostTechnologies(postID uint, techNames []string) ([]model.Technology, error) {
	techs, err := repository.FindOrCreateTechnologiesByNames(techNames)
	if err != nil {
		return nil, err
	}
	if err := repository.ReplacePostTechnologies(postID, techs); err != nil {
		return nil, err
	}
	return techs, nil
}

func (s *PostService) TechnologiesByPostID(postID uint) ([]model.Technology, error) {
	return repository.GetTechnologiesByPostID(postID)
}

func (s *PostService) LikePost(postID, userID uint) error {
	_, err := repository.GetLikeByUserAndPost(userID, postID)
	if err == nil {
		return nil
	}
	if err != nil && err.Error() != "record not found" {
		return err
	}

	like := model.Like{
		PostID: postID,
		UserID: userID,
	}

	return repository.CreateLike(&like)
}

func (s *PostService) UnlikePost(postID, userID uint) error {
	return repository.DeleteLike(userID, postID)
}

func (s *PostService) GetPostParticipants(postID uint) ([]model.User, error) {
	return repository.GetPostParticipants(postID)
}
