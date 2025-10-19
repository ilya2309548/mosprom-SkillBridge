package repository

import (
	"errors"
	"mosprom/api/internal/db"
	"mosprom/api/internal/model"

	"gorm.io/gorm"
)

func GetAllUsers() ([]model.User, error) {
	var users []model.User
	err := db.DB.Preload("Technologies").Preload("Directions").Find(&users).Error
	return users, err
}

// GetAllUsersLight returns users with only fields needed for rating calculation
func GetAllUsersLight() ([]model.User, error) {
	var users []model.User
	// select minimal columns: id and achievements (array)
	err := db.DB.Select("id, achievements").Find(&users).Error
	return users, err
}

func CreateUser(user *model.User) error {
	return db.DB.Create(user).Error
}

func GetUserByID(id uint) (model.User, error) {
	var user model.User
	err := db.DB.Preload("Technologies").Preload("Directions").First(&user, id).Error
	return user, err
}

func UpdateUser(user *model.User) error {
	// Save handles both update of fields and M2M associations when set
	return db.DB.Session(&gorm.Session{FullSaveAssociations: true}).Save(user).Error
}

func DeleteUser(id uint) error {
	return db.DB.Delete(&model.User{}, id).Error
}

func FindOrCreateTechnologiesByNames(names []string) ([]model.Technology, error) {
	if len(names) == 0 {
		return nil, nil
	}
	var techs []model.Technology
	for _, n := range names {
		if n == "" {
			continue
		}
		var t model.Technology
		if err := db.DB.Where("name = ?", n).First(&t).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				t = model.Technology{Name: n}
				if err := db.DB.Create(&t).Error; err != nil {
					return nil, err
				}
			} else {
				return nil, err
			}
		}
		techs = append(techs, t)
	}
	return techs, nil
}

func FindOrCreateDirectionsByNames(names []string) ([]model.Direction, error) {
	if len(names) == 0 {
		return nil, nil
	}
	var dirs []model.Direction
	for _, n := range names {
		if n == "" {
			continue
		}
		var d model.Direction
		if err := db.DB.Where("name = ?", n).First(&d).Error; err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				d = model.Direction{Name: n}
				if err := db.DB.Create(&d).Error; err != nil {
					return nil, err
				}
			} else {
				return nil, err
			}
		}
		dirs = append(dirs, d)
	}
	return dirs, nil
}

func ReplaceUserAssociations(user *model.User, technologies []model.Technology, directions []model.Direction) error {
	if err := db.DB.Model(user).Association("Technologies").Replace(&technologies); err != nil {
		return err
	}
	if err := db.DB.Model(user).Association("Directions").Replace(&directions); err != nil {
		return err
	}
	return nil
}

func GetUserByTelegram(tg string) (model.User, error) {
	var user model.User
	err := db.DB.Where("telegram_name = ?", tg).First(&user).Error
	return user, err
}

func ReplaceUserTechnologies(userID uint, technologies []model.Technology) error {
	var user model.User
	if err := db.DB.First(&user, userID).Error; err != nil {
		return err
	}
	return db.DB.Model(&user).Association("Technologies").Replace(&technologies)
}

// GetTechnologiesByUserID returns technologies linked to the given user via user_technologies
func GetTechnologiesByUserID(userID uint) ([]model.Technology, error) {
	var techs []model.Technology
	err := db.DB.Model(&model.Technology{}).
		Joins("JOIN user_technologies ut ON ut.technology_id = technologies.id").
		Where("ut.user_id = ?", userID).
		Find(&techs).Error
	return techs, err
}
func AddAchievementToUser(userID uint, achievement string) error {
	return db.DB.Model(&model.User{}).
		Where("id = ?", userID).
		Update("achievements", gorm.Expr("array_append(achievements, ?)", achievement)).
		Error
}

func GetUserAchievements(userID uint) ([]string, error) {
	var user model.User
	err := db.DB.Select("achievements").Where("id = ?", userID).First(&user).Error
	return user.Achievements, err
}

// GetUsersByIDsLight returns minimal user fields useful for recommendations
func GetUsersByIDsLight(ids []uint) ([]model.User, error) {
	if len(ids) == 0 {
		return []model.User{}, nil
	}
	var users []model.User
	err := db.DB.Select("id, telegram_name, name, description, photo, university, rating").
		Where("id IN ?", ids).Find(&users).Error
	return users, err
}

// GetParticipationCounts returns map of user_id -> events count from post_participants and the maximum events among all users
func GetParticipationCounts() (map[uint]int64, int64, error) {
	type row struct {
		UserID uint  `gorm:"column:user_id"`
		Cnt    int64 `gorm:"column:cnt"`
	}
	var rows []row
	err := db.DB.Table("post_participants").
		Select("user_id, COUNT(*) as cnt").
		Group("user_id").
		Find(&rows).Error
	if err != nil {
		return nil, 0, err
	}
	m := make(map[uint]int64, len(rows))
	var max int64 = 0
	for _, r := range rows {
		m[r.UserID] = r.Cnt
		if r.Cnt > max {
			max = r.Cnt
		}
	}
	return m, max, nil
}

// UpdateUserRating updates only the rating column for a user
func UpdateUserRating(userID uint, rating float64) error {
	return db.DB.Model(&model.User{}).Where("id = ?", userID).Update("rating", rating).Error
}
