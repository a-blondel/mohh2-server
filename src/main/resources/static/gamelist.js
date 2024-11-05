// gamelist.js

class GameListManager {
    constructor() {
        // Configuration
        this.API_ENDPOINT = '/games/api';
        this.REFRESH_INTERVAL = 30000; // 30 seconds

        // DOM Elements
        this.autoRefreshToggle = document.getElementById('auto-refresh-toggle');
        this.lastUpdateSpan = document.getElementById('last-update');
        this.errorMessage = document.getElementById('error-message');
        this.loadingDiv = document.getElementById('loading');
        this.gamesContainer = document.getElementById('games-container');
        this.noGamesDiv = document.getElementById('no-games');

        // Templates
        this.gameCardTemplate = document.getElementById('game-card-template');
        this.playerItemTemplate = document.getElementById('player-item-template');

        // State
        this.refreshInterval = null;
        this.isLoading = false;

        // Initialize auto-refresh toggle state
        this.autoRefreshToggle.checked = true;

        // Bind event listeners
        this.autoRefreshToggle.addEventListener('change', this.handleAutoRefreshToggle.bind(this));

        // Initial load and start auto-refresh
        this.loadData();
        this.startAutoRefresh();
    }

    // Data fetching
    async loadData() {
        if (this.isLoading) return;

        try {
            this.setLoading(true);
            const response = await fetch(this.API_ENDPOINT);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.updateUI(data);
            this.updateLastRefreshTime();
            this.hideError();
        } catch (error) {
            console.error('Error fetching data:', error);
            this.showError();
        } finally {
            this.setLoading(false);
        }
    }

    // UI Updates
    updateUI(data) {
        this.updateStatistics(data.stats);
        this.updateGamesList(data.activeGames);
    }

    updateStatistics(stats) {
        document.getElementById('active-games').textContent = stats.activeGames;
        document.getElementById('players-in-game').textContent = stats.playersInGame;
        document.getElementById('players-in-lobby').textContent = stats.playersInLobby;
        document.getElementById('total-players').textContent = stats.totalPlayers;
    }

    updateGamesList(games) {
        this.gamesContainer.innerHTML = ''; // Clear existing games

        if (games.length === 0) {
            this.noGamesDiv.style.display = 'block';
            return;
        }

        this.noGamesDiv.style.display = 'none';
        games.forEach(game => this.createGameCard(game));
    }

    createGameCard(game) {
        const card = this.gameCardTemplate.content.cloneNode(true);

        // Update game info
        card.querySelector('.game-name').textContent = game.name;
        card.querySelector('.game-version').textContent = game.version;
        card.querySelector('.game-start-time').textContent = this.formatTime(game.startTime);
        card.querySelector('.player-count').textContent = game.activePlayers.length;
        card.querySelector('.max-players').textContent = game.maxPlayers;

        // Add players
        const playerList = card.querySelector('.player-list');
        game.activePlayers.forEach(player => {
            const playerItem = this.createPlayerItem(player);
            playerList.appendChild(playerItem);
        });

        this.gamesContainer.appendChild(card);
    }

    createPlayerItem(player) {
        const item = this.playerItemTemplate.content.cloneNode(true);

        item.querySelector('.player-name').textContent = player.name;
        item.querySelector('.player-time').textContent = player.playTime;

        // Show/hide host badge
        const hostBadge = item.querySelector('.host-badge');
        if (!player.isHost) {
            hostBadge.remove();
        }

        return item;
    }

    // Time formatting
    formatTime(isoString) {
        const date = new Date(isoString);
        return date.toLocaleString();
    }

    updateLastRefreshTime() {
        const now = new Date();
        this.lastUpdateSpan.textContent = `Last updated: ${now.toLocaleString()}`;
    }

    // Loading state
    setLoading(isLoading) {
        this.isLoading = isLoading;
        this.loadingDiv.style.display = isLoading ? 'block' : 'none';
    }

    // Error handling
    showError() {
        this.errorMessage.style.display = 'block';
        this.noGamesDiv.style.display = 'none';
    }

    hideError() {
        this.errorMessage.style.display = 'none';
    }

    // Auto-refresh handling
    handleAutoRefreshToggle(event) {
        if (event.target.checked) {
            this.startAutoRefresh();
        } else {
            this.stopAutoRefresh();
        }
    }

    startAutoRefresh() {
        if (!this.refreshInterval) {
            this.refreshInterval = setInterval(() => this.loadData(), this.REFRESH_INTERVAL);
        }
    }

    stopAutoRefresh() {
        if (this.refreshInterval) {
            clearInterval(this.refreshInterval);
            this.refreshInterval = null;
        }
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.gameListManager = new GameListManager();
});