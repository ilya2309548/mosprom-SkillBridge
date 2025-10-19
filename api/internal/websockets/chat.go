package websockets

import (
	"2gis-calm-map/api/internal/service"
	"context"
	"encoding/json"
	"log"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/gorilla/websocket"
)

const (
	// Max wait time when writing message to peer
	writeWait = 10 * time.Second

	// Max time till next pong from peer
	pongWait = 60 * time.Second

	// Send ping interval, must be less then pong wait time
	pingPeriod = (pongWait * 9) / 10

	// Maximum message size allowed from peer.
	maxMessageSize = 10000
)

var (
	newline = []byte{'\n'}
)

// Run our websocket server, accepting various requests
func Run(ctx context.Context) error {
	<-ctx.Done()
	return ctx.Err()
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		// origin := r.Header.Get("Origin")
		// return origin == "http://localhost:3000"
		return true
	},
}

type Message struct {
	Type      string          `json:"type"`
	Text      string          `json:"text"`
	From      json.RawMessage `json:"from"`
	To        json.RawMessage `json:"to"`
	UserID    string          `json:"userId"`
	ChatID    string          `json:"chatId"`
	Title     string          `json:"title,omitempty"`
	History   []Message       `json:"history,omitempty"`
	Timestamp time.Time       `json:"timestamp"`
}

type Session struct {
	clients   map[*websocket.Conn]ClientInfo
	broadcast chan Message
	mutex     sync.Mutex
	history   []Message
	title     string
}

type ClientInfo struct {
	UserID string
}

var chatSessions = make(map[uuid.UUID]*Session)
var chatSessionsMutex = &sync.Mutex{}
var userService = service.NewUserService()
var clubService = service.NewClubService()

type Client struct {
	conn *websocket.Conn
	send chan []byte
}

func newClient(conn *websocket.Conn) *Client {
	return &Client{
		conn: conn,
		send: make(chan []byte, 256),
	}
}

func (client *Client) readPump(chatSession *Session) {
	defer func() {
		chatSession.mutex.Lock()
		delete(chatSession.clients, client.conn)
		chatSession.mutex.Unlock()
		client.conn.Close()
		log.Println("readPump finished for client")
	}()

	client.conn.SetReadLimit(maxMessageSize)
	client.conn.SetReadDeadline(time.Now().Add(pongWait))
	client.conn.SetPongHandler(func(string) error {
		client.conn.SetReadDeadline(time.Now().Add(pongWait))
		return nil
	})

	// Start endless read loop, waiting for messages from client
	for {
		_, jsonMessage, err := client.conn.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
				log.Printf("Unexpected close error in readPump: %v", err)
			} else {
				log.Printf("Error reading message in readPump: %v", err)
			}
			break
		}

		var message Message
		if err := json.Unmarshal(jsonMessage, &message); err != nil {
			log.Printf("Error unmarshaling message: %v", err)
			continue
		}

		if message.Type == "join" {
			// Validate that the user exists in the database
			if userID, err := strconv.Atoi(message.UserID); err == nil {
				if _, err := userService.GetUserByID(uint(userID)); err != nil {
					// User doesn't exist, close connection
					log.Printf("User with ID %s does not exist, closing connection", message.UserID)
					client.conn.WriteMessage(websocket.TextMessage, []byte(`{"type":"error","text":"User does not exist"}`))
					break
				}
			} else {
				// Invalid user ID format, close connection
				log.Printf("Invalid user ID format: %s, closing connection", message.UserID)
				client.conn.WriteMessage(websocket.TextMessage, []byte(`{"type":"error","text":"Invalid user ID format"}`))
				break
			}

			chatSession.mutex.Lock()
			chatSession.clients[client.conn] = ClientInfo{
				UserID: message.UserID,
			}
			chatSession.mutex.Unlock()

			chatSession.broadcast <- message
			continue
		}

		chatSession.broadcast <- message
	}
}

func (client *Client) writePump() {
	ticker := time.NewTicker(pingPeriod)
	defer func() {
		ticker.Stop()
		client.conn.Close()
	}()
	for {
		select {
		case message, ok := <-client.send:
			client.conn.SetWriteDeadline(time.Now().Add(writeWait))
			if !ok {
				client.conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			w, err := client.conn.NextWriter(websocket.TextMessage)
			if err != nil {
				return
			}
			w.Write(message)

			n := len(client.send)
			for range n {
				w.Write(newline)
				w.Write(<-client.send)
			}

			if err := w.Close(); err != nil {
				log.Printf("Error closing writer in writePump: %v", err)
				return
			}
		case <-ticker.C:
			client.conn.SetWriteDeadline(time.Now().Add(writeWait))
			if err := client.conn.WriteMessage(websocket.PingMessage, nil); err != nil {
				log.Printf("Error sending ping in writePump: %v", err)
				return
			}
		}
	}
}

// ServeWs handles websocket requests from clients requests.
func ServeWs(w http.ResponseWriter, r *http.Request) {
	chatIDStr := r.URL.Query().Get("chat_id")
	if chatIDStr == "" {
		log.Println("chat_id is required")
		http.Error(w, "chat_id is required", http.StatusBadRequest)
		return
	}

	chatID, err := uuid.Parse(chatIDStr)
	if err != nil {
		log.Printf("Invalid chat_id value: %v", err)
		http.Error(w, "Invalid chat_id value", http.StatusBadRequest)
		return
	}

	chatSessionsMutex.Lock()
	chatSession, ok := chatSessions[chatID]
	if !ok {
		log.Printf("Creating new chat session for session_id: %s", chatID)
		// Get club by chat ID to set the title
		club, err := clubService.GetByChatID(chatIDStr)
		title := "Chat"
		if err == nil {
			title = club.Name + " Chat"
		}

		chatSession = &Session{
			clients:   make(map[*websocket.Conn]ClientInfo),
			broadcast: make(chan Message, 100),
			history:   []Message{},
			title:     title,
		}
		chatSessions[chatID] = chatSession

		go chatSession.HandleMessages()
	}
	chatSessionsMutex.Unlock()

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("Failed to upgrade connection: %v", err)
		return
	}
	log.Println("WebSocket connection upgraded successfully")

	// Send chat history to the newly connected client
	chatSession.mutex.Lock()
	historyMessage := Message{
		Type:    "history",
		History: chatSession.history,
		Title:   chatSession.title,
	}
	log.Printf("Sending history message: %+v", historyMessage)
	if err := conn.WriteJSON(historyMessage); err != nil {
		log.Printf("Error sending history message: %v", err)
		chatSession.mutex.Unlock()
		conn.Close()
		return
	}

	// Add the connection to the session's clients map with empty info initially
	// The info will be updated when the user sends a "join" message
	chatSession.clients[conn] = ClientInfo{}
	chatSession.mutex.Unlock()

	client := newClient(conn)

	go client.writePump()
	go client.readPump(chatSession)
}

func (chatSsn *Session) HandleMessages() {
	for message := range chatSsn.broadcast {
		chatSsn.mutex.Lock()

		message.Timestamp = time.Now()

		if message.Type == "join" {
			// Get user's nickname from user service
			var nickname string
			if userID, err := strconv.Atoi(message.UserID); err == nil {
				if user, err := userService.GetUserByID(uint(userID)); err == nil {
					nickname = user.TelegramName
					if nickname == "" {
						nickname = user.Name
					}
				}
			}

			if nickname == "" {
				nickname = "Anonymous"
			}

			joinMessage := Message{
				Type:      "system",
				Text:      nickname + " joined the chat",
				ChatID:    message.ChatID,
				Timestamp: time.Now(),
			}

			chatSsn.history = append(chatSsn.history, joinMessage)

			if len(chatSsn.history) > 100 {
				chatSsn.history = chatSsn.history[1:]
			}

			messageBytes, err := json.Marshal(joinMessage)
			if err != nil {
				log.Printf("Error marshaling join message: %v", err)
				chatSsn.mutex.Unlock()
				continue
			}

			for client := range chatSsn.clients {
				err := client.WriteMessage(websocket.TextMessage, messageBytes)
				if err != nil {
					log.Printf("Failed to send join message to client, closing connection: %v", err)
					client.Close()
					delete(chatSsn.clients, client)
				}
			}

			chatSsn.mutex.Unlock()
			continue
		}

		if message.Type == "chat" {
			// Get user's nickname from user service
			var senderNickname string
			if userID, err := strconv.Atoi(message.UserID); err == nil {
				if user, err := userService.GetUserByID(uint(userID)); err == nil {
					senderNickname = user.TelegramName
					if senderNickname == "" {
						senderNickname = user.Name
					}
				}
			}

			if senderNickname == "" {
				senderNickname = "Anonymous"
			}

			messageData := make(map[string]any)
			tempMessageBytes, _ := json.Marshal(message)
			json.Unmarshal(tempMessageBytes, &messageData)
			messageData["nickname"] = senderNickname
			messageBytes, _ := json.Marshal(messageData)

			chatSsn.history = append(chatSsn.history, message)

			if len(chatSsn.history) > 100 {
				chatSsn.history = chatSsn.history[1:]
			}

			// Send the message with nickname to all clients
			for client := range chatSsn.clients {
				err := client.WriteMessage(websocket.TextMessage, messageBytes)
				if err != nil {
					log.Printf("Failed to send message to client, closing connection: %v", err)
					client.Close()
					delete(chatSsn.clients, client)
				}
			}
		} else if message.Type != "join" {
			messageBytes, err := json.Marshal(message)
			if err != nil {
				log.Printf("Error marshaling message: %v", err)
				chatSsn.mutex.Unlock()
				continue
			}

			for client := range chatSsn.clients {
				err := client.WriteMessage(websocket.TextMessage, messageBytes)
				if err != nil {
					log.Printf("Failed to send message to client, closing connection: %v", err)
					client.Close()
					delete(chatSsn.clients, client)
				}
			}
		}

		chatSsn.mutex.Unlock()
	}
}
