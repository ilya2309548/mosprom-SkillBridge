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
}

type Technology struct {
	ID   uint   `json:"id" gorm:"primaryKey"`
	Name string `json:"name" gorm:"uniqueIndex;not null"`
}

type Direction struct {
	ID   uint   `json:"id" gorm:"primaryKey"`
	Name string `json:"name" gorm:"uniqueIndex;not null"`
}
