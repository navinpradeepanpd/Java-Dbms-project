/**
 * CineBook Web Frontend Utilities & API Handlers
 */

const API = {
    getMovies: () => fetch('/api/movies').then(r => r.json()),
    getShows: () => fetch('/api/shows').then(r => r.json()),
    getShow: (id) => fetch(`/api/shows/${id}`).then(r => r.json()),
    getSeatMap: (showId) => fetch(`/api/shows/${showId}/seats`).then(r => r.json()),
    bookTickets: (data) => fetch('/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }).then(async r => {
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Failed to complete booking');
        return json;
    }),
    getBooking: (id) => fetch(`/api/bookings/${id}`).then(async r => {
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Booking not found');
        return json;
    }),
    getCustomerBookings: (phone) => fetch(`/api/bookings/customer/${phone}`).then(r => r.json()),
    cancelBooking: (id) => fetch(`/api/bookings/${id}/cancel`, {
        method: 'POST'
    }).then(async r => {
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Failed to cancel booking');
        return json;
    })
};

// Pricing Helper matching Java SeatType enum
const SeatPricing = {
    REGULAR: 150.0,
    PREMIUM: 250.0,
    VIP: 400.0,
    getPriceForRow(rowChar) {
        const upper = rowChar.toUpperCase();
        if (upper === 'A' || upper === 'B') return this.REGULAR;
        if (upper === 'C' || upper === 'D') return this.PREMIUM;
        if (upper === 'E') return this.VIP;
        return 0;
    },
    getTypeForRow(rowChar) {
        const upper = rowChar.toUpperCase();
        if (upper === 'A' || upper === 'B') return 'regular';
        if (upper === 'C' || upper === 'D') return 'premium';
        if (upper === 'E') return 'vip';
        return 'regular';
    }
};

// UI Toast Notification helper
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.5s';
        setTimeout(() => toast.remove(), 500);
    }, 4000);
}

// Format Currency
function formatCurrency(amount) {
    return `Rs. ${Number(amount).toFixed(2)}`;
}

// Format Date/Time helper
function formatDateTime(dateStr, timeStr) {
    return `${dateStr} at ${timeStr}`;
}

