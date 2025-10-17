package handler

import (
	"log"
	"net/http"
	"os"
	"time"

	"2gis-calm-map/api/internal/service"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

var userServiceAuth = service.NewUserService()

type LoginRequest struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required"`
}

type LoginResponse struct {
	Token string `json:"token"`
}

// Login godoc
// @Summary User login
// @Description Authenticates user and returns JWT token
// @Tags auth
// @Accept json
// @Produce json
// @Param input body LoginRequest true "Login credentials (email & password)"
// @Success 200 {object} LoginResponse
// @Failure 400 {object} map[string]string "Invalid input"
// @Failure 401 {object} map[string]string "Unauthorized"
// @Failure 500 {object} map[string]string "Server error"
// @Router /login [post]
func Login(c *gin.Context) {
	var req LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	log.Printf("login attempt email=%s", req.Email)
	user, err := userServiceAuth.AuthenticateUser(req.Email, req.Password)
	if err != nil {
		log.Printf("login failed email=%s: %v", req.Email, err)
		c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid credentials"})
		return
	}

	secret := os.Getenv("JWT_SECRET")
	if secret == "" {
		secret = "secret"
	}

	claims := jwt.MapClaims{
		"user_id": user.ID,
		"name":    user.Name, // This line remains unchanged as it is still needed for the claims
		"email":   user.Email,
		"role":    user.Role,
		"exp":     time.Now().Add(24 * time.Hour).Unix(),
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenStr, err := token.SignedString([]byte(secret))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "could not generate token"})
		return
	}

	c.JSON(http.StatusOK, LoginResponse{Token: tokenStr})
}
