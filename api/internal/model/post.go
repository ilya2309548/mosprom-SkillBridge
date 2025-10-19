package model

import (
	"time"
)

type PostType string

const (
	InfoPost    PostType = "informational" // информационный пост
	Project     PostType = "project"       // проект
	Internship  PostType = "internship"    // стажировка
	Educational PostType = "educational"   // образовательный
	Activity    PostType = "activity"      // мероприятие
	Vacancy     PostType = "vacancy"       // вакансия
)

type PostFormat string

const (
	InPerson PostFormat = "in_person"
	Online   PostFormat = "online"
)

type Post struct {
	ID                uint         `json:"id" gorm:"primaryKey"`
	Title             string       `json:"title" gorm:"not null"`
	Description       string       `json:"description"`
	Type              PostType     `json:"type" gorm:"type:varchar(20);not null"`
	StartDate         *time.Time   `json:"start_date"`
	EndDate           *time.Time   `json:"end_date"`
	AgeRestriction    *int         `json:"age_restriction"`
	Format            *PostFormat  `json:"format" gorm:"type:varchar(20)"`
	Address           string       `json:"address"`
	ClubID            uint         `json:"club_id" gorm:"not null"`
	Club              *Club        `json:"club" gorm:"constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	Participants      []User       `json:"participants" gorm:"many2many:post_participants;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	ParticipantsCount int          `json:"participants_count"`
	Technologies      []Technology `json:"technologies" gorm:"many2many:post_technologies;constraint:OnUpdate:CASCADE,OnDelete:CASCADE;"`
	Likes             []Like       `json:"likes" gorm:"constraint:OnUpdate:CASCADE,OnDelete:CASCADE;"`
	CreatedAt         time.Time    `json:"created_at"`
	UpdatedAt         time.Time    `json:"updated_at"`
}
