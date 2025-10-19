package handler

import (
	"mosprom/api/internal/model"
	"mosprom/api/internal/service"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type PostHandler struct {
	postService *service.PostService
}

func NewPostHandler(postService *service.PostService) *PostHandler {
	return &PostHandler{
		postService: postService,
	}
}

func isValidPostType(postType model.PostType) bool {
	switch postType {
	case model.InfoPost, model.Project, model.Internship, model.Educational, model.Activity, model.Vacancy:
		return true
	default:
		return false
	}
}

func isValidPostFormat(postFormat model.PostFormat) bool {
	switch postFormat {
	case model.InPerson, model.Online:
		return true
	default:
		return false
	}
}

// JoinPostRequest represents the request body for joining a post
type JoinPostRequest struct {
	PostID uint `json:"post_id" binding:"required"`
	UserID uint `json:"user_id" binding:"required"`
}

// Join allows a user to join a post (e.g., activity)
// @Summary User joins a post
// @Tags posts
// @Accept json
// @Produce json
// @Param input body handler.JoinPostRequest true "Post and User IDs"
// @Success 200 {object} model.Post
// @Failure 400 {object} map[string]string
// @Router /posts/join [post]
// @Security BearerAuth
func (h *PostHandler) Join(c *gin.Context) {
	var body JoinPostRequest
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	post, err := h.postService.Join(body.UserID, body.PostID)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, post)
}

// CreatePost creates a new post
// @Summary Create a new post
// @Description Create a new post with the provided details
// @Tags posts
// @Accept json
// @Produce json
// @Param post body service.CreatePostInput true "Post details"
// @Success 201 {object} model.Post
// @Failure 400 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts [post]
func (h *PostHandler) CreatePost(c *gin.Context) {
	var input service.CreatePostInput
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Validate post type
	if !isValidPostType(input.Type) {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post type"})
		return
	}

	// Validate format if provided
	if input.Format != nil && !isValidPostFormat(*input.Format) {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post format"})
		return
	}

	post, err := h.postService.CreatePost(input)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, post)
}

// GetPostByID retrieves a post by its ID
// @Summary Get post by ID
// @Description Get a post by its ID
// @Tags posts
// @Produce json
// @Param id path int true "Post ID"
// @Success 200 {object} model.Post
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id} [get]
func (h *PostHandler) GetPostByID(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}

	post, err := h.postService.GetPostByID(uint(id))
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Post not found"})
		return
	}

	c.JSON(http.StatusOK, post)
}

// GetPostsByClubID retrieves all posts for a specific club
// @Summary Get posts by club ID
// @Description Get all posts for a specific club
// @Tags posts
// @Produce json
// @Param club_id query int true "Club ID"
// @Success 200 {array} model.Post
// @Failure 400 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/club [get]
func (h *PostHandler) GetPostsByClubID(c *gin.Context) {
	clubIDParam := c.Query("club_id")
	clubID, err := strconv.Atoi(clubIDParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid club ID"})
		return
	}

	posts, err := h.postService.GetPostsByClubID(uint(clubID))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, posts)
}

// GetAllPosts retrieves all posts
// @Summary Get all posts
// @Description Get all posts
// @Tags posts
// @Produce json
// @Success 200 {array} model.Post
// @Failure 500 {object} map[string]string
// @Router /posts [get]
func (h *PostHandler) GetAllPosts(c *gin.Context) {
	posts, err := h.postService.GetAllPosts()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, posts)
}

// JoinedByMe lists posts the current authorized user joined
// @Summary List my joined posts
// @Tags posts
// @Security BearerAuth
// @Produce json
// @Success 200 {array} model.Post
// @Failure 401 {object} map[string]string
// @Router /me/posts [get]
func (h *PostHandler) JoinedByMe(c *gin.Context) {
	uidAny, ok := c.Get("user_id")
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	uid := uidAny.(uint)
	posts, err := h.postService.JoinedPosts(uid)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, posts)
}

// JoinedByUser lists posts a given user joined
// @Summary List posts joined by user
// @Tags posts
// @Produce json
// @Param id path int true "User ID"
// @Success 200 {array} model.Post
// @Failure 400 {object} map[string]string
// @Router /users/{id}/posts [get]
func (h *PostHandler) JoinedByUser(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}
	posts, err := h.postService.JoinedPosts(uint(id))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, posts)
}

// UpdatePost updates an existing post
// @Summary Update a post
// @Description Update an existing post with the provided details
// @Tags posts
// @Accept json
// @Produce json
// @Param id path int true "Post ID"
// @Param post body service.UpdatePostInput true "Post details"
// @Success 200 {object} model.Post
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id} [put]
// @Security BearerAuth
func (h *PostHandler) UpdatePost(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}

	var input service.UpdatePostInput
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	input.ID = uint(id)

	if input.Type != nil && !isValidPostType(*input.Type) {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post type"})
		return
	}

	if input.Format != nil && !isValidPostFormat(*input.Format) {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post format"})
		return
	}

	post, err := h.postService.UpdatePost(input)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Post not found"})
		return
	}

	c.JSON(http.StatusOK, post)
}

// DeletePost deletes a post by its ID
// @Summary Delete a post
// @Description Delete a post by its ID
// @Tags posts
// @Produce json
// @Param id path int true "Post ID"
// @Success 204
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id} [delete]
// @Security BearerAuth
func (h *PostHandler) DeletePost(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}

	if err := h.postService.DeletePost(uint(id)); err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Post not found"})
		return
	}

	c.Status(http.StatusNoContent)
}

// LikePost adds a like to a post

// PostTechnologiesRequest represents input for setting technologies of a post
type PostTechnologiesRequest struct {
	Technologies []string `json:"technologies" binding:"required"`
}

// SetTechnologies replaces technologies for a post by ID
// @Summary Set technologies for a post
// @Tags posts
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param id path int true "Post ID"
// @Param input body handler.PostTechnologiesRequest true "Technologies"
// @Success 200 {array} model.Technology
// @Failure 400 {object} map[string]string
// @Router /posts/{id}/technologies [post]
func (h *PostHandler) SetTechnologies(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}
	var body PostTechnologiesRequest
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	techs, err := h.postService.SetPostTechnologies(uint(id), body.Technologies)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, techs)
}

// GetTechnologies returns technologies for a post by ID
// @Summary Get technologies for a post
// @Tags posts
// @Security BearerAuth
// @Produce json
// @Param id path int true "Post ID"
// @Success 200 {array} model.Technology
// @Failure 400 {object} map[string]string
// @Router /posts/{id}/technologies [get]
func (h *PostHandler) GetTechnologies(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}
	techs, err := h.postService.TechnologiesByPostID(uint(id))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, techs)
}

// @Summary Like a post
// @Description Add a like to a post by the current user
// @Tags posts
// @Produce json
// @Param id path int true "Post ID"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id}/like [post]
// @Security BearerAuth
func (h *PostHandler) LikePost(c *gin.Context) {
	// Get post ID from URL parameter
	postIDParam := c.Param("id")
	postID, err := strconv.Atoi(postIDParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}

	// Get user ID from context (assuming it's set by authentication middleware)
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	// Convert userID to uint
	userIDUint, ok := userID.(uint)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Invalid user ID type"})
		return
	}

	// Add like to post
	if err := h.postService.LikePost(uint(postID), userIDUint); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Post liked successfully"})
}

// UnlikePost removes a like from a post
// @Summary Unlike a post
// @Description Remove a like from a post by the current user
// @Tags posts
// @Produce json
// @Param id path int true "Post ID"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id}/unlike [post]
// @Security BearerAuth
func (h *PostHandler) UnlikePost(c *gin.Context) {
	// Get post ID from URL parameter
	postIDParam := c.Param("id")
	postID, err := strconv.Atoi(postIDParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}

	// Get user ID from context (assuming it's set by authentication middleware)
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "User not authenticated"})
		return
	}

	// Convert userID to uint
	userIDUint, ok := userID.(uint)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Invalid user ID type"})
		return
	}

	// Remove like from post
	if err := h.postService.UnlikePost(uint(postID), userIDUint); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Post unliked successfully"})
}

// GetPostParticipants retrieves participants for a post
// @Summary Get participants for a post
// @Description Get participants for a post by post ID
// @Tags posts
// @Produce json
// @Param id path int true "Post ID"
// @Success 200 {array} model.User
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id}/participants [get]
func (h *PostHandler) GetPostParticipants(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}

	users, err := h.postService.GetPostParticipants(uint(id))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	for i := range users {
		users[i].Password = ""
	}

	c.JSON(http.StatusOK, users)
}

// RecommendedUsersForPost returns recommended users for a post
// @Summary Recommended users for post
// @Description Returns users sorted by score = alpha*TechMatch + beta*UserRatingNorm
// @Tags posts
// @Produce json
// @Param id path int true "Post ID"
// @Param alpha query number false "Weight for TechMatch (default 0.6)"
// @Param beta query number false "Weight for UserRatingNorm (default 0.4)"
// @Success 200 {array} service.RecommendedUser
// @Failure 400 {object} map[string]string
// @Router /posts/{id}/recommended_users [get]
func (h *PostHandler) RecommendedUsersForPost(c *gin.Context) {
	idParam := c.Param("id")
	id, err := strconv.Atoi(idParam)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid post ID"})
		return
	}
	alpha := 0.6
	beta := 0.4
	if v := c.Query("alpha"); v != "" {
		if f, err := strconv.ParseFloat(v, 64); err == nil {
			alpha = f
		}
	}
	if v := c.Query("beta"); v != "" {
		if f, err := strconv.ParseFloat(v, 64); err == nil {
			beta = f
		}
	}
	res, err := h.postService.RecommendedUsersForPost(uint(id), alpha, beta)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, res)
}

// RecommendedPostsForMe returns posts sorted by technology match with current user
// @Summary Recommended posts for me (by technologies)
// @Description Returns posts sorted by TechMatch = |UserTech ∩ EventTech| / |EventTech|
// @Tags posts
// @Security BearerAuth
// @Produce json
// @Success 200 {array} service.RecommendedPost
// @Failure 401 {object} map[string]string
// @Router /me/posts/recommended [get]
func (h *PostHandler) RecommendedPostsForMe(c *gin.Context) {
	uidAny, ok := c.Get("user_id")
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	uid := uidAny.(uint)
	res, err := h.postService.RecommendedForUser(uid)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, res)
}
