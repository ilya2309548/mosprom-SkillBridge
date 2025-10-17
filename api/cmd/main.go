package main

import (
	"log"
	"net/http"
	"strings"

	"2gis-calm-map/api/config"
	docs "2gis-calm-map/api/docs"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"

	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/handler"
)

// @title 2gis-calm-map API
// @version 1.0
// @description This is a sample server.
// @host localhost:8080
// @BasePath /
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
func main() {
	cfg := config.LoadConfig()
	db.Init(cfg)

	r := gin.Default()

	// Simple CORS (allow all) – adjust for production.
	r.Use(func(c *gin.Context) {
		origin := c.GetHeader("Origin")
		if origin == "" {
			origin = "*"
		}
		c.Header("Access-Control-Allow-Origin", origin)
		c.Header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept")
		c.Header("Access-Control-Allow-Credentials", "true")
		if c.Request.Method == http.MethodOptions {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	// Swagger route (covers /swagger, /swagger/, /swagger/index.html, etc.)
	r.GET("/swagger/*any", func(c *gin.Context) {
		seg := c.Param("any")        // includes leading '/' unless empty
		if seg == "" || seg == "/" { // /swagger or /swagger/
			c.Redirect(http.StatusTemporaryRedirect, "/swagger/index.html")
			return
		}
		h := c.Request.Host
		if h != "" && !strings.EqualFold(h, docs.SwaggerInfo.Host) {
			docs.SwaggerInfo.Host = h
		}
		ginSwagger.WrapHandler(swaggerFiles.Handler)(c)
	})

	r.POST("/register", handler.Register)
	r.POST("/login", handler.Login)
	r.GET("/users", handler.GetUsers)

	log.Println("start at :8080")
	if err := r.Run(":8080"); err != nil {
		log.Fatal(err)
	}
}
