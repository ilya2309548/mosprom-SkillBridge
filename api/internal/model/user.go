package model

import (
	"github.com/lib/pq"
)

// User — основная сущность пользователя
type User struct {
	ID           uint           `json:"id" gorm:"primaryKey"`
	TelegramName string         `json:"telegram_name" gorm:"uniqueIndex;not null"`
	Name         string         `json:"name"`
	Password     string         `json:"-"` // не отдаём наружу
	Description  string         `json:"description"`
	Photo        string         `json:"photo"` // относительный путь до файла
	Achievements pq.StringArray `json:"achievements" gorm:"type:text[]" swaggertype:"array,string"`
	EventsCount  int            `json:"events_count"`
	University   string         `json:"university"`
	Technologies []Technology   `json:"technologies" gorm:"many2many:user_technologies;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	Directions   []Direction    `json:"directions" gorm:"many2many:user_directions;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	// Subscriptions
	Clubs      []Club `json:"clubs" gorm:"many2many:club_subscribers;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	ClubsCount int    `json:"clubs_count"`
	// Post participation (e.g., activities, projects, etc.)
	Posts []Post `json:"posts" gorm:"many2many:post_participants;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	Likes []Like `json:"likes" gorm:"constraint:OnUpdate:CASCADE,OnDelete:CASCADE;"`
	// Rating — вычисляемый показатель вовлеченности пользователя [0..10]
	Rating float64 `json:"rating" gorm:"type:double precision;default:0"`
}

type Technology struct {
	ID         uint        `json:"id" gorm:"primaryKey"`
	Name       string      `json:"name" gorm:"uniqueIndex;not null"`
	Directions []Direction `json:"directions" gorm:"many2many:direction_technologies;constraint:OnUpdate:CASCADE,OnDelete:CASCADE;"`
}

type Direction struct {
	ID           uint         `json:"id" gorm:"primaryKey"`
	Name         string       `json:"name" gorm:"uniqueIndex;not null"`
	Technologies []Technology `json:"technologies" gorm:"many2many:direction_technologies;constraint:OnUpdate:CASCADE,OnDelete:CASCADE;"`
}
