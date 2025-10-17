FROM golang:1.25-alpine AS builder
WORKDIR /src

# Install build tools (optional):
RUN apk add --no-cache git ca-certificates && update-ca-certificates

# Copy only module files first for better layer caching
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the source
COPY . .

# Disable CGO for static binary and set build-time vars
ENV CGO_ENABLED=0 GOOS=linux GOARCH=amd64
RUN go build -trimpath -ldflags "-s -w" -o /bin/app ./cmd/main.go

FROM gcr.io/distroless/base-debian12:nonroot AS runtime
WORKDIR /app
COPY --from=builder /bin/app ./app
# Copy swagger docs if present (not strictly needed in runtime if served statically later)
COPY docs ./docs
USER nonroot:nonroot
EXPOSE 8080

ENTRYPOINT ["/app/app"]