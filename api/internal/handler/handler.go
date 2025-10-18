package handler

import (
	"2gis-calm-map/api/internal/service"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

var userService = service.NewUserService()
var directionService = service.NewDirectionService()

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

// GetMe godoc
// @Summary Get current user profile
// @Description Returns current authorized user profile
// @Tags profile
// @Security BearerAuth
// @Produce json
// @Success 200 {object} model.User
// @Failure 401 {object} map[string]string
// @Router /me [get]
func GetMe(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	uid, _ := uidAny.(uint)
	user, err := userService.GetUserByID(uid)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
		return
	}
	user.Password = ""
	c.JSON(http.StatusOK, user)
}

// UpdateMe godoc
// @Summary Update current user profile
// @Description Update current authorized user profile, accepts JSON or multipart/form-data
// @Tags profile
// @Security BearerAuth
// @Accept json
// @Produce json
// @Success 200 {object} model.User
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Router /me [put]
func UpdateMe(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	uid, _ := uidAny.(uint)

	ct := c.ContentType()
	input := service.UpdateUserInput{ID: uid}
	var photoPath *string

	if strings.HasPrefix(ct, "multipart/form-data") {
		if v := c.PostForm("telegram_name"); v != "" {
			input.TelegramName = &v
		}
		if v := c.PostForm("name"); v != "" {
			input.Name = &v
		}
		if v := c.PostForm("password"); v != "" {
			input.Password = &v
		}
		if v := c.PostForm("description"); v != "" {
			input.Description = &v
		}
		if v := c.PostForm("university"); v != "" {
			input.University = &v
		}
		if v := c.PostForm("events_count"); v != "" {
			if n, err := strconv.Atoi(v); err == nil {
				input.EventsCount = &n
			}
		}
		if v := c.PostForm("achievements"); v != "" {
			a := parseCSVList(v)
			input.Achievements = &a
		}
		if v := c.PostForm("technologies"); v != "" {
			t := parseCSVList(v)
			input.Technologies = &t
		}
		if v := c.PostForm("directions"); v != "" {
			d := parseCSVList(v)
			input.Directions = &d
		}
		if file, err := c.FormFile("photo"); err == nil && file != nil {
			p, err := savePhoto(file)
			if err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
				return
			}
			photoPath = &p
		}
	} else {
		var body map[string]interface{}
		if err := c.ShouldBindJSON(&body); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		if v, ok := body["telegram_name"].(string); ok {
			input.TelegramName = &v
		}
		if v, ok := body["name"].(string); ok {
			input.Name = &v
		}
		if v, ok := body["password"].(string); ok {
			input.Password = &v
		}
		if v, ok := body["description"].(string); ok {
			input.Description = &v
		}
		if v, ok := body["university"].(string); ok {
			input.University = &v
		}
		if v, ok := body["events_count"].(float64); ok {
			n := int(v)
			input.EventsCount = &n
		}
		if v, ok := body["achievements"].([]any); ok {
			input.Achievements = sliceAnyToStringPtr(v)
		}
		if v, ok := body["technologies"].([]any); ok {
			input.Technologies = sliceAnyToStringPtr(v)
		}
		if v, ok := body["directions"].([]any); ok {
			input.Directions = sliceAnyToStringPtr(v)
		}
	}

	if photoPath != nil {
		input.PhotoPath = photoPath
	}
	user, err := userService.UpdateUser(input)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	user.Password = ""
	c.JSON(http.StatusOK, user)
}

// SetMyPhoto godoc
// @Summary Set current user photo
// @Description Upload and set user photo (multipart/form-data)
// @Tags profile
// @Security BearerAuth
// @Accept mpfd
// @Produce json
// @Param photo formData file true "User photo"
// @Success 200 {object} model.User
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Router /me/photo [post]
func SetMyPhoto(c *gin.Context) {
	uidAny, _ := c.Get("user_id")
	uid, _ := uidAny.(uint)
	file, err := c.FormFile("photo")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "photo is required"})
		return
	}
	p, err := savePhoto(file)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	input := service.UpdateUserInput{ID: uid, PhotoPath: &p}
	user, err := userService.UpdateUser(input)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	user.Password = ""
	c.JSON(http.StatusOK, user)
}

// CreateUser handles multipart/form-data with optional photo and JSON fields
// Accepts either multipart/form-data or application/json
func CreateUser(c *gin.Context) {
	ct := c.ContentType()
	var input service.CreateUserInput

	var achievements []string
	var technologies []string
	var directions []string

	// Photo handling
	var photoPath string

	if strings.HasPrefix(ct, "multipart/form-data") {
		// Text fields
		input.TelegramName = c.PostForm("telegram_name")
		input.Name = c.PostForm("name")
		input.Password = c.PostForm("password")
		input.Description = c.PostForm("description")
		input.University = c.PostForm("university")
		if ec, err := strconv.Atoi(c.PostForm("events_count")); err == nil {
			input.EventsCount = ec
		}
		achievements = parseCSVList(c.PostForm("achievements"))
		technologies = parseCSVList(c.PostForm("technologies"))
		directions = parseCSVList(c.PostForm("directions"))

		file, err := c.FormFile("photo")
		if err == nil && file != nil {
			p, err := savePhoto(file)
			if err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
				return
			}
			photoPath = p // it's just filename now
		}
	} else {
		var body struct {
			TelegramName string   `json:"telegram_name" binding:"required"`
			Name         string   `json:"name"`
			Password     string   `json:"password" binding:"required"`
			Description  string   `json:"description"`
			University   string   `json:"university"`
			EventsCount  int      `json:"events_count"`
			Achievements []string `json:"achievements"`
			Technologies []string `json:"technologies"`
			Directions   []string `json:"directions"`
			PhotoBase64  string   `json:"photo_base64"` // опционально
		}
		if err := c.ShouldBindJSON(&body); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		input.TelegramName = body.TelegramName
		input.Name = body.Name
		input.Password = body.Password
		input.Description = body.Description
		input.University = body.University
		input.EventsCount = body.EventsCount
		achievements = body.Achievements
		technologies = body.Technologies
		directions = body.Directions
		// For simplicity, we ignore PhotoBase64 here, or could implement decoding
	}

	input.Achievements = achievements
	input.Technologies = technologies
	input.Directions = directions
	input.PhotoPath = photoPath

	user, err := userService.CreateUser(input)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	user.Password = "" // don't expose
	c.JSON(http.StatusOK, user)
}

func GetUserByID(c *gin.Context) {
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	user, err := userService.GetUserByID(uint(id64))
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	user.Password = ""
	c.JSON(http.StatusOK, user)
}

func UpdateUser(c *gin.Context) {
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}

	ct := c.ContentType()
	input := service.UpdateUserInput{ID: uint(id64)}
	var photoPath *string

	if strings.HasPrefix(ct, "multipart/form-data") {
		if v := c.PostForm("telegram_name"); v != "" {
			input.TelegramName = &v
		}
		if v := c.PostForm("name"); v != "" {
			input.Name = &v
		}
		if v := c.PostForm("password"); v != "" {
			input.Password = &v
		}
		if v := c.PostForm("description"); v != "" {
			input.Description = &v
		}
		if v := c.PostForm("university"); v != "" {
			input.University = &v
		}
		if v := c.PostForm("events_count"); v != "" {
			if n, err := strconv.Atoi(v); err == nil {
				input.EventsCount = &n
			}
		}

		if v := c.PostForm("achievements"); v != "" {
			a := parseCSVList(v)
			input.Achievements = &a
		}
		if v := c.PostForm("technologies"); v != "" {
			t := parseCSVList(v)
			input.Technologies = &t
		}
		if v := c.PostForm("directions"); v != "" {
			d := parseCSVList(v)
			input.Directions = &d
		}

		file, err := c.FormFile("photo")
		if err == nil && file != nil {
			p, err := savePhoto(file)
			if err != nil {
				c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
				return
			}
			photoPath = &p
		}
	} else {
		var body map[string]interface{}
		if err := c.ShouldBindJSON(&body); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}
		// selectively map
		if v, ok := body["telegram_name"].(string); ok {
			input.TelegramName = &v
		}
		if v, ok := body["name"].(string); ok {
			input.Name = &v
		}
		if v, ok := body["password"].(string); ok {
			input.Password = &v
		}
		if v, ok := body["description"].(string); ok {
			input.Description = &v
		}
		if v, ok := body["university"].(string); ok {
			input.University = &v
		}
		if v, ok := body["events_count"].(float64); ok {
			n := int(v)
			input.EventsCount = &n
		}
		if v, ok := body["achievements"].([]any); ok {
			input.Achievements = sliceAnyToStringPtr(v)
		}
		if v, ok := body["technologies"].([]any); ok {
			input.Technologies = sliceAnyToStringPtr(v)
		}
		if v, ok := body["directions"].([]any); ok {
			input.Directions = sliceAnyToStringPtr(v)
		}
	}

	if photoPath != nil {
		input.PhotoPath = photoPath
	}

	user, err := userService.UpdateUser(input)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	user.Password = ""
	c.JSON(http.StatusOK, user)
}

func DeleteUser(c *gin.Context) {
	id64, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id"})
		return
	}
	if err := userService.DeleteUser(uint(id64)); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.Status(http.StatusNoContent)
}

// Utils
func parseCSVList(s string) []string {
	if s == "" {
		return nil
	}
	parts := strings.Split(s, ",")
	res := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			res = append(res, p)
		}
	}
	return res
}

func sliceAnyToStringPtr(v []any) *[]string {
	if len(v) == 0 {
		empty := []string{}
		return &empty
	}
	out := make([]string, 0, len(v))
	for _, it := range v {
		if s, ok := it.(string); ok && s != "" {
			out = append(out, s)
		}
	}
	return &out
}

func savePhoto(fileHeader *multipart.FileHeader) (string, error) {
	// Ensure directory exists
	base := "uploads/photos"
	if err := os.MkdirAll(base, 0o755); err != nil {
		return "", err
	}
	// Use timestamped name to avoid collisions
	name := fileHeader.Filename
	ext := filepath.Ext(name)
	safe := strings.TrimSuffix(filepath.Base(name), ext)
	if safe == "" {
		safe = "photo"
	}
	filename := safe + "_" + strconv.FormatInt(timeNowUnix(), 10) + ext
	full := filepath.Join(base, filename)
	if err := saveUploadedFile(fileHeader, full); err != nil {
		return "", err
	}
	// Return only filename; consumers should use /photos/{filename}
	return filename, nil
}

func saveUploadedFile(fh *multipart.FileHeader, dst string) error {
	// Gin provides a helper via Context.SaveUploadedFile but we are outside ctx here
	// We'll manually copy
	src, err := fh.Open()
	if err != nil {
		return err
	}
	defer src.Close()
	out, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer func() { _ = out.Close() }()
	if _, err := io.Copy(out, src); err != nil {
		return err
	}
	return nil
}

func timeNowUnix() int64 { return time.Now().Unix() }

// GetPhotoByName godoc
// @Summary Get photo by filename
// @Description Returns a photo from uploads/photos by filename (DB stores just filenames)
// @Tags photos
// @Produce octet-stream
// @Param filename path string true "Photo file name"
// @Success 200 {file} file
// @Failure 404 {object} map[string]string
// @Router /photos/{filename} [get]
func GetPhotoByName(c *gin.Context) {
	name := c.Param("filename")
	// Prevent path traversal
	name = filepath.Base(name)
	if name == "." || name == "" {
		c.JSON(http.StatusNotFound, gin.H{"error": "not found"})
		return
	}
	full := filepath.Join("uploads", "photos", name)
	if _, err := os.Stat(full); err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "not found"})
		return
	}
	c.File(full)
}

// ListDirections godoc
// @Summary List all directions
// @Tags directories
// @Produce json
// @Success 200 {array} model.Direction
// @Router /directions [get]
func ListDirections(c *gin.Context) {
	dirs, err := directionService.List()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, dirs)
}
