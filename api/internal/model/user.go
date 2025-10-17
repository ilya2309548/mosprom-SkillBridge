package model

type User struct {
	ID       uint   `json:"id" gorm:"primaryKey"`
	Name     string `json:"name"`
	Email    string `json:"email"`
	Password string `json:"-"`    // не отдаём наружу!
	Role     string `json:"role"` // например, "user", "admin"
}
