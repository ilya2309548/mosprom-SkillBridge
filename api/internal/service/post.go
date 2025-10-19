package service

import (
	"errors"
	"mosprom/api/internal/model"
	"mosprom/api/internal/repository"
	"time"

	"gorm.io/gorm"
)

type PostService struct{}

func NewPostService() *PostService {
	return &PostService{}
}

func (s *PostService) Join(userID, postID uint) (model.Post, error) {
	if err := repository.JoinUserToPost(userID, postID); err != nil {
		return model.Post{}, err
	}
	// Return the updated post with participants
	return repository.GetPostByID(postID)
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
	if !errors.Is(err, gorm.ErrRecordNotFound) {
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

type RecommendedPost struct {
	Post      model.Post `json:"post"`
	TechMatch float64    `json:"tech_match"`
}

// RecommendedForUser returns posts sorted by technology match with the user
func (s *PostService) RecommendedForUser(userID uint) ([]RecommendedPost, error) {
	rows, err := repository.GetPostTechMatchRanking(userID)
	if err != nil {
		return nil, err
	}
	ids := make([]uint, 0, len(rows))
	for _, r := range rows {
		ids = append(ids, r.PostID)
	}
	m, err := repository.GetPostsByIDs(ids)
	if err != nil {
		return nil, err
	}
	out := make([]RecommendedPost, 0, len(rows))
	for _, r := range rows {
		if p, ok := m[r.PostID]; ok {
			out = append(out, RecommendedPost{Post: p, TechMatch: r.TechMatch})
		}
	}
	return out, nil
}

type RecommendedUser struct {
	UserID     uint    `json:"user_id"`
	Score      float64 `json:"score"`
	TechMatch  float64 `json:"tech_match"`
	RatingNorm float64 `json:"rating_norm"`
}

// RecommendedUsersForPost returns users sorted by score for a given post
// alpha and beta are weights; defaults (if zero) are alpha=0.6, beta=0.4
func (s *PostService) RecommendedUsersForPost(postID uint, alpha, beta float64) ([]RecommendedUser, error) {
	if alpha == 0 && beta == 0 {
		alpha, beta = 0.6, 0.4
	}
	rows, err := repository.GetRecommendedUsersForPost(postID, alpha, beta)
	if err != nil {
		return nil, err
	}
	out := make([]RecommendedUser, 0, len(rows))
	for _, r := range rows {
		out = append(out, RecommendedUser{
			UserID:     r.UserID,
			Score:      r.Score,
			TechMatch:  r.Tech,
			RatingNorm: r.Rn,
		})
	}
	return out, nil
}
