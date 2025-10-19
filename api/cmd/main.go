package main

import (
	"context"
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
	"2gis-calm-map/api/internal/middleware"
	"2gis-calm-map/api/internal/service"
	"2gis-calm-map/api/internal/websockets"
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

	// Start websocket server in a separate goroutine
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	go func() {
		if err := websockets.Run(ctx); err != nil {
			log.Printf("Websocket server error: %v", err)
		}
	}()
	db.Init(cfg)

	// Initialize services
	postService := service.NewPostService()
	postHandler := handler.NewPostHandler(postService)

	r := gin.Default()

	// serve uploaded files
	r.Static("/uploads", "uploads")

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

	// Auth routes (kept but not focus of current task)
	r.POST("/register", handler.Register)
	r.POST("/login", handler.Login)

	// Users CRUD
	r.GET("/users", handler.GetUsers)
	r.POST("/users", handler.CreateUser)
	r.GET("/users/:id", handler.GetUserByID)
	r.PUT("/users/:id", handler.UpdateUser)
	r.PATCH("/users/:id", handler.UpdateUser)
	r.DELETE("/users/:id", handler.DeleteUser)
	// POST fetch technologies by user ID
	r.POST("/users/technologies", handler.PostUserTechnologies)
	// User technologies
	r.GET("/users/:id/technologies", handler.GetUserTechnologies)

	// Public photos access by filename
	r.GET("/photos/:filename", handler.GetPhotoByName)

	// Clubs
	r.GET("/clubs", handler.ListClubs)
	r.GET("/clubs/:name", handler.GetClubByName)
	r.GET("/clubs/:name/chat", handler.GetClubChatID)
	// Subscribers of a club (avoid conflict with /clubs/:name)
	r.GET("/clubs/id/:id/subscribers", handler.GetClubSubscribers)

	// Directions
	r.GET("/directions", handler.ListDirections)
	r.GET("/directions/:id/technologies", handler.GetTechnologiesByDirection)

	// Posts
	r.GET("/posts", postHandler.GetAllPosts)
	r.GET("/posts/:id", postHandler.GetPostByID)
	r.GET("/posts/club", postHandler.GetPostsByClubID)
	r.POST("/posts", postHandler.CreatePost)

	// Secured post routes
	postAuth := r.Group("/posts")
	postAuth.Use(middleware.JWTAuth())
	{
		postAuth.PUT("/:id", postHandler.UpdatePost)
		postAuth.DELETE("/:id", postHandler.DeletePost)

		postAuth.POST("/join", postHandler.Join)
		// Post likes
		postAuth.POST("/:id/like", postHandler.LikePost)
		postAuth.POST("/:id/unlike", postHandler.UnlikePost)
	}

	clubAuth := r.Group("/clubs")
	clubAuth.Use(middleware.JWTAuth())
	{
		clubAuth.POST("", handler.CreateClub)
		clubAuth.POST(":id/logo", handler.SetClubLogo)
		clubAuth.POST("id/:id/subscribe", handler.SubscribeToClub)
	}

	// Secured profile routes
	auth := r.Group("/")
	auth.Use(middleware.JWTAuth())
	{
		auth.GET("/me", handler.GetMe)
		auth.PUT("/me", handler.UpdateMe)
		auth.PATCH("/me", handler.UpdateMe)
		auth.POST("/me/photo", handler.SetMyPhoto)
		// Current user's subscribed clubs
		auth.GET("/me/clubs", handler.GetUserClubs)
		// Current user's joined posts
		auth.GET("/me/posts", postHandler.JoinedByMe)
	}

	// Get clubs of a specific user by id
	r.GET("/users/:id/clubs", handler.GetSubscriberClubs)
	// Posts joined by a user
	r.GET("/users/:id/posts", postHandler.JoinedByUser)

	r.GET("/ws", func(c *gin.Context) {
		websockets.ServeWs(c.Writer, c.Request)
	})

	log.Println("start at :8080")
	if err := r.Run(":8080"); err != nil {
		log.Fatal(err)
	}
}
