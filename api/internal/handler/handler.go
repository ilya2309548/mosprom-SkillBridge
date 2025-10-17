package handler

import (
	"2gis-calm-map/api/internal/service"
	"net/http"

	"github.com/gin-gonic/gin"
)

var userService = service.NewUserService()

// GetUsers godoc
// @Summary Get users
// @Tags users
// @Produce json
// @Success 200 {array} model.User
// @Router /users [get]
func GetUsers(c *gin.Context) {
	users, err := userService.GetAllUsers()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, users)
}
