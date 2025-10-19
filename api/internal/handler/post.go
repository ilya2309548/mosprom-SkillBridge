package handler

import (
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/service"
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
// @Success 204
// @Failure 400 {object} map[string]string
// @Router /posts/join [post]
func (h *PostHandler) Join(c *gin.Context) {
	var body JoinPostRequest
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	if err := h.postService.Join(body.UserID, body.PostID); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.Status(http.StatusNoContent)
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
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /posts/{id} [delete]
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

	c.JSON(http.StatusOK, gin.H{"message": "Post deleted successfully"})
}

func isValidPostType(postType model.PostType) bool {
	validTypes := []model.PostType{
		model.InfoPost,
		model.Project,
		model.Internship,
		model.Educational,
		model.Activity,
		model.Vacancy,
	}

	for _, validType := range validTypes {
		if postType == validType {
			return true
		}
	}
	return false
}

func isValidPostFormat(postFormat model.PostFormat) bool {
	return postFormat == model.InPerson || postFormat == model.Online
}
