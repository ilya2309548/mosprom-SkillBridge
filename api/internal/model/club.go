package model

// Club represents a club entity
type Club struct {
	ID          uint        `json:"id" gorm:"primaryKey"`
	Name        string      `json:"name" gorm:"uniqueIndex;not null"`
	Logo        string      `json:"logo"` // filename only
	Description string      `json:"description"`
	CreatorID   uint        `json:"creator_id"`
	Creator     *User       `json:"creator" gorm:"constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	Directions  []Direction `json:"directions"`
	Events      []Event     `json:"events"`
}

// Event is a simple event entity belonging to a club
type Event struct {
	ID          uint   `json:"id" gorm:"primaryKey"`
	ClubID      uint   `json:"club_id"`
	Name        string `json:"name"`
	Description string `json:"description"`
}
