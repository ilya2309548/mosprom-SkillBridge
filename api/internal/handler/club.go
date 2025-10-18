package handler

import (
	"2gis-calm-map/api/internal/middleware"
	"2gis-calm-map/api/internal/model"
	"2gis-calm-map/api/internal/service"
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

// ListClubs godoc
// @Summary List clubs by directions
// @Tags clubs
// @Produce json
// @Param directions query string false "Comma-separated direction names"
// @Success 200 {array} model.Club
// @Router /clubs [get]
func ListClubs(c *gin.Context) {
	dirs := c.Query("directions")
	var names []string
	if dirs != "" {
		names = strings.Split(dirs, ",")
	}
	for i := range names {
		names[i] = strings.TrimSpace(names[i])
	}
	clubs, err := clubService.ListByDirections(names)
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
