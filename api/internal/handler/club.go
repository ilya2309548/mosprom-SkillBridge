package handler

import (
	"mosprom/api/internal/middleware"
	"mosprom/api/internal/model"
	"mosprom/api/internal/service"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
)

var clubService = service.NewClubService()

// ClubCreateRequest is the body for creating a club
type ClubCreateRequest struct {
	Name        string   `json:"name" binding:"required"`
	Description string   `json:"description"`
	Directions  []string `json:"directions"`
}

// CreateClub godoc
// @Summary Create club
// @Tags clubs
// @Security BearerAuth
// @Accept json
// @Produce json
// @Param input body ClubCreateRequest true "Club data"
// @Success 200 {object} model.Club
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Router /clubs [post]
func CreateClub(c *gin.Context) {
	uidAny, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	uid := uidAny.(uint)
	var body ClubCreateRequest
	if err := c.ShouldBindJSON(&body); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	club, err := clubService.CreateClub(service.CreateClubInput{Name: body.Name, Description: body.Description, DirectionNames: body.Directions}, uid)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, club)
}

// SetClubLogo godoc
// @Summary Set club logo
// @Tags clubs
// @Security BearerAuth
// @Accept multipart/form-data
// @Produce json
// @Param id path int true "Club ID"
// @Param logo formData file true "Logo file"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Router /clubs/{id}/logo [post]
func SetClubLogo(c *gin.Context) {
	uidAny, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	_ = uidAny.(uint)
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	file, err := c.FormFile("logo")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "logo is required"})
		return
	}
	filename, err := savePhoto(file)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	if err := clubService.SetLogo(uint(id64), filename); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"logo": filename})
}

// GetClubByName godoc
// @Summary Get club by name
// @Tags clubs
// @Produce json
// @Param name path string true "Club name"
// @Success 200 {object} model.Club
// @Failure 404 {object} map[string]string
// @Router /clubs/{name} [get]
func GetClubByName(c *gin.Context) {
	name := c.Param("name")
	name = strings.TrimSpace(name)
	club, err := clubService.GetByName(name)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, club)
}

// GetClubChatID godoc
// @Summary Get club chat ID by club name
// @Tags clubs
// @Produce json
// @Param name path string true "Club name"
// @Success 200 {object} map[string]string
// @Failure 404 {object} map[string]string
// @Router /clubs/{name}/chat [get]
func GetClubChatID(c *gin.Context) {
	name := c.Param("name")
	name = strings.TrimSpace(name)
	club, err := clubService.GetByName(name)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"chat_id": club.ChatID})
}

// ListClubs godoc
// @Summary List clubs with optional name and directions filters
// @Description If name is provided, filter by exact club name. If directions provided (comma-separated), return clubs that have ALL these directions. If omitted, no filter by that field.
// @Tags clubs
// @Produce json
// @Param name query string false "Exact club name"
// @Param directions query string false "Comma-separated direction names"
// @Success 200 {array} model.Club
// @Router /clubs [get]
func ListClubs(c *gin.Context) {
	nameQ := strings.TrimSpace(c.Query("name"))
	var namePtr *string
	if nameQ != "" {
		namePtr = &nameQ
	}

	dirs := c.Query("directions")
	var names []string
	if dirs != "" {
		names = strings.Split(dirs, ",")
	}
	for i := range names {
		names[i] = strings.TrimSpace(names[i])
	}

	clubs, err := clubService.ListFiltered(namePtr, names)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, clubs)
}

// SubscribeToClub godoc
// @Summary Subscribe current user to a club
// @Tags clubs
// @Security BearerAuth
// @Param id path int true "Club ID"
// @Success 204
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Router /clubs/id/{id}/subscribe [post]
func SubscribeToClub(c *gin.Context) {
	uidAny, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	uid := uidAny.(uint)
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	if err := clubService.Subscribe(uid, uint(id64)); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.Status(http.StatusNoContent)
}

// GetClubSubscribers godoc
// @Summary List subscribers of a club
// @Tags clubs
// @Produce json
// @Param id path int true "Club ID"
// @Success 200 {array} model.User
// @Failure 400 {object} map[string]string
// @Router /clubs/id/{id}/subscribers [get]
func GetClubSubscribers(c *gin.Context) {
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	users, err := clubService.Subscribers(uint(id64))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, users)
}

// GetUserClubs godoc
// @Summary List clubs the current user is subscribed to
// @Tags profile
// @Security BearerAuth
// @Produce json
// @Success 200 {array} model.Club
// @Failure 401 {object} map[string]string
// @Router /me/clubs [get]
func GetUserClubs(c *gin.Context) {
	uidAny, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	uid := uidAny.(uint)
	clubs, err := clubService.ClubsOfUser(uid)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, clubs)
}

// GetSubscriberClubs godoc
// @Summary List clubs a user is subscribed to
// @Tags users
// @Produce json
// @Param id path int true "User ID"
// @Success 200 {array} model.Club
// @Failure 400 {object} map[string]string
// @Router /users/{id}/clubs [get]
func GetSubscriberClubs(c *gin.Context) {
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	clubs, err := clubService.ClubsOfUser(uint(id64))
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, clubs)
}

func init() {
	_ = middleware.JWTAuth // keep import if unused by compiler
	var _ model.Club       // reference for swagger type import
}
