package repository

import (
	"2gis-calm-map/api/internal/db"
	"2gis-calm-map/api/internal/model"

	"gorm.io/gorm"
)

func CreatePost(post *model.Post) error {
	return db.DB.Create(post).Error
}

func GetPostByID(id uint) (model.Post, error) {
	var post model.Post
	err := db.DB.Preload("Club").Preload("Likes").Preload("Technologies").First(&post, id).Error
	return post, err
}

func GetPostsByClubID(clubID uint) ([]model.Post, error) {
	var posts []model.Post
	err := db.DB.Where("club_id = ?", clubID).Preload("Club").Preload("Likes").Preload("Technologies").Find(&posts).Error
	return posts, err
}

func GetAllPosts() ([]model.Post, error) {
	var posts []model.Post
	err := db.DB.Preload("Club").Preload("Likes").Preload("Technologies").Find(&posts).Error
	return posts, err
}

func UpdatePost(post *model.Post) error {
	return db.DB.Save(post).Error
}

func DeletePost(id uint) error {
	return db.DB.Delete(&model.Post{}, id).Error
}

func GetPostWithDetails(id uint) (model.Post, error) {
	var post model.Post
	err := db.DB.Preload("Club").Preload("Likes.User").Preload("Technologies").First(&post, id).Error
	return post, err
}

// JoinUserToPost adds user to post participants and updates counters atomically
func JoinUserToPost(userID, postID uint) error {
	return db.DB.Transaction(func(tx *gorm.DB) error {
		var post model.Post
		if err := tx.First(&post, postID).Error; err != nil {
			return err
		}
		var user model.User
		if err := tx.First(&user, userID).Error; err != nil {
			return err
		}

		// check existing
		var cnt int64
		if err := tx.Table("post_participants").Where("post_id = ? AND user_id = ?", postID, userID).Count(&cnt).Error; err != nil {
			return err
		}
		if cnt == 0 {
			if err := tx.Model(&post).Association("Participants").Append(&user); err != nil {
				return err
			}
			if err := tx.Model(&model.Post{}).Where("id = ?", postID).UpdateColumn("participants_count", gorm.Expr("participants_count + 1")).Error; err != nil {
				return err
			}
			if err := tx.Model(&model.User{}).Where("id = ?", userID).UpdateColumn("events_count", gorm.Expr("events_count + 1")).Error; err != nil { // reuse events_count as overall participation counter
				return err
			}
		}
		return nil
	})
}

func GetUserJoinedPosts(userID uint) ([]model.Post, error) {
	var posts []model.Post
	err := db.DB.Model(&model.Post{}).
		Joins("JOIN post_participants pp ON pp.post_id = posts.id").
		Where("pp.user_id = ?", userID).
		Preload("Club").Preload("Technologies").
		Find(&posts).Error
	return posts, err
}

func ReplacePostTechnologies(postID uint, techs []model.Technology) error {
	var post model.Post
	if err := db.DB.First(&post, postID).Error; err != nil {
		return err
	}
	return db.DB.Model(&post).Association("Technologies").Replace(&techs)
}

func GetTechnologiesByPostID(postID uint) ([]model.Technology, error) {
	var techs []model.Technology
	err := db.DB.Model(&model.Technology{}).
		Joins("JOIN post_technologies pt ON pt.technology_id = technologies.id").
		Where("pt.post_id = ?", postID).
		Find(&techs).Error
	return techs, err
}
func GetPostParticipants(postID uint) ([]model.User, error) {
	var users []model.User
	err := db.DB.Model(&model.Post{}).
		Where("posts.id = ?", postID).
		Joins("JOIN post_participants pp ON pp.post_id = posts.id").
		Joins("JOIN users ON users.id = pp.user_id").
		Find(&users).Error
	return users, err
}
