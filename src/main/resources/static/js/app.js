/**
 * CineBook 2.0 – Core Frontend Client & Cinema Application Engine
 * Connects directly to Spring Boot REST Controllers & MySQL JDBC backend
 */

// Universal API Client
const API = {
    getMovies: async () => {
        const r = await fetch('/api/movies');
        if (!r.ok) throw new Error(`Server returned HTTP ${r.status} while fetching movies`);
        return r.json();
    },
    getShows: async () => {
        const r = await fetch('/api/shows');
        if (!r.ok) throw new Error(`Server returned HTTP ${r.status} while fetching showtimes`);
        return r.json();
    },
    getShow: async (id) => {
        const r = await fetch(`/api/shows/${id}`);
        if (!r.ok) throw new Error(`Show #${id} not found`);
        return r.json();
    },
    getSeatMap: async (showId) => {
        const r = await fetch(`/api/shows/${showId}/seats`);
        if (!r.ok) throw new Error(`Failed to load seat layout for show #${showId}`);
        return r.json();
    },
    bookTickets: async (data) => {
        const r = await fetch('/api/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Booking transaction failed');
        return json;
    },
    getBooking: async (id) => {
        const r = await fetch(`/api/bookings/${id}`);
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Booking reference not found');
        return json;
    },
    getCustomerBookings: async (phone) => {
        const r = await fetch(`/api/bookings/customer/${phone}`);
        if (!r.ok) throw new Error('Customer lookup failed');
        return r.json();
    },
    cancelBooking: async (id) => {
        const r = await fetch(`/api/bookings/${id}/cancel`, { method: 'POST' });
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Cancellation request failed');
        return json;
    }
};

// Rich Cinematic Movie Metadata (Maps seamlessly to real backend DB records)
// Stored globally on window so it is guaranteed to be available across all scripts
window.MovieMetadata = {
    'Inception': {
        poster: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1600&q=80',
        rating: '8.8',
        votes: '2.4M',
        director: 'Christopher Nolan',
        cast: 'Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page',
        tagline: 'Your mind is the scene of the crime.',
        synopsis: 'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.',
        ageCert: 'UA 13+',
        trailerUrl: 'https://www.youtube.com/watch?v=YoHD9XEInc0',
        palette: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, #0284c7 100%)',
        symbol: '🌀'
    },
    'Interstellar': {
        poster: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1600&q=80',
        rating: '8.7',
        votes: '1.9M',
        director: 'Christopher Nolan',
        cast: 'Matthew McConaughey, Anne Hathaway, Jessica Chastain',
        tagline: 'Mankind was born on Earth. It was never meant to die here.',
        synopsis: 'When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft, along with a team of researchers, to find a new planet for humans through a mysterious wormhole.',
        ageCert: 'UA 13+',
        trailerUrl: 'https://www.youtube.com/watch?v=zSWdZVtXT7E',
        palette: 'linear-gradient(135deg, #09090b 0%, #312e81 50%, #6366f1 100%)',
        symbol: '🚀'
    },
    'The Dark Knight': {
        poster: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1600&q=80',
        rating: '9.0',
        votes: '2.8M',
        director: 'Christopher Nolan',
        cast: 'Christian Bale, Heath Ledger, Aaron Eckhart',
        tagline: 'Why so serious?',
        synopsis: 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.',
        ageCert: 'UA 16+',
        trailerUrl: 'https://www.youtube.com/watch?v=EXeTwQWrcwY',
        palette: 'linear-gradient(135deg, #09090b 0%, #27272a 50%, #eab308 100%)',
        symbol: '🦇'
    },
    'Oppenheimer': {
        poster: 'https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1475274047050-1d0c0975c63e?w=1600&q=80',
        rating: '8.9',
        votes: '780K',
        director: 'Christopher Nolan',
        cast: 'Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.',
        tagline: 'The world forever changes.',
        synopsis: 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during the Manhattan Project, examining the triumph and subsequent political tragedy of the nuclear age.',
        ageCert: 'A 18+',
        trailerUrl: 'https://www.youtube.com/watch?v=uYPbbksJxIg',
        palette: 'linear-gradient(135deg, #1c1917 0%, #7c2d12 50%, #f97316 100%)',
        symbol: '⚛️'
    },
    'Avatar: The Way of Water': {
        poster: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1600&q=80',
        rating: '7.6',
        votes: '520K',
        director: 'James Cameron',
        cast: 'Sam Worthington, Zoe Saldana, Sigourney Weaver',
        tagline: 'Return to Pandora.',
        synopsis: 'Jake Sully lives with his newfound family formed on the extrasolar moon Pandora. Once a familiar threat returns to finish what was previously started, Jake must work with Neytiri and the army of the Na\'vi race to protect their home.',
        ageCert: 'UA 13+',
        trailerUrl: 'https://www.youtube.com/watch?v=d9MyW72ELq0',
        palette: 'linear-gradient(135deg, #022c22 0%, #065f46 50%, #06b6d4 100%)',
        symbol: '🌊'
    },
    'Gladiator II': {
        poster: 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=1600&q=80',
        rating: '8.1',
        votes: '240K',
        director: 'Ridley Scott',
        cast: 'Paul Mescal, Pedro Pascal, Denzel Washington',
        tagline: 'What we do in life echoes in eternity.',
        synopsis: 'Years after witnessing the death of Maximus at the hands of his uncle, Lucius must enter the Colosseum after his home is conquered by the tyrannical Emperors who now lead Rome with an iron fist.',
        ageCert: 'A 18+',
        trailerUrl: 'https://www.youtube.com/watch?v=4rgYUipGJNo',
        palette: 'linear-gradient(135deg, #18181b 0%, #831843 50%, #e11d48 100%)',
        symbol: '⚔️'
    },
    'Spider-Man: Across the Spider-Verse': {
        poster: 'https://images.unsplash.com/photo-1635805737707-575885ab0820?w=800&q=80',
        backdrop: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1600&q=80',
        rating: '8.7',
        votes: '390K',
        director: 'Joaquim Dos Santos, Kemp Powers',
        cast: 'Shameik Moore, Hailee Steinfeld, Oscar Isaac',
        tagline: 'It\'s how you wear the mask that matters.',
        synopsis: 'Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence. When the heroes clash on how to handle a new threat, Miles must redefine what it means to be a hero.',
        ageCert: 'U',
        trailerUrl: 'https://www.youtube.com/watch?v=cqGjhVJWtEg',
        palette: 'linear-gradient(135deg, #0f172a 0%, #581c87 50%, #d946ef 100%)',
        symbol: '🕷️'
    }
};

// Safe helper that ALWAYS returns a valid metadata object and never throws
window.getMovieMeta = function(movieName) {
    if (window.MovieMetadata && window.MovieMetadata[movieName]) {
        return window.MovieMetadata[movieName];
    }
    return {
        poster: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80',
        backdrop: null,
        rating: '8.5',
        votes: '500K',
        director: 'Acclaimed Director',
        cast: 'Stellar Ensemble Cast',
        tagline: 'Experience this cinematic premiere.',
        synopsis: 'Experience this movie in crystal-clear projection and immersive Dolby Atmos surround sound at CineBook Cinemas.',
        ageCert: 'UA',
        palette: 'linear-gradient(135deg, #0f172a 0%, #334155 100%)',
        symbol: '🎬'
    };
};

// Also expose as local variable for direct access
const MovieMetadata = window.MovieMetadata;
const getMovieMeta = window.getMovieMeta;

// Seat Pricing Reference (Strictly preserves backend SeatType enum rules)
const SeatPricing = {
    REGULAR: 150.0,
    PREMIUM: 250.0,
    VIP: 400.0,
    getPriceForRow(rowChar) {
        const upper = (rowChar || '').toUpperCase();
        if (upper === 'A' || upper === 'B') return this.REGULAR;
        if (upper === 'C' || upper === 'D') return this.PREMIUM;
        if (upper === 'E') return this.VIP;
        return 0;
    },
    getTypeForRow(rowChar) {
        const upper = (rowChar || '').toUpperCase();
        if (upper === 'A' || upper === 'B') return 'regular';
        if (upper === 'C' || upper === 'D') return 'premium';
        if (upper === 'E') return 'vip';
        return 'regular';
    }
};

window.SeatPricing = SeatPricing;
window.formatCurrency = formatCurrency;
window.showToast = showToast;
window.openAuthModal = openAuthModal;
window.openMovieDetailsModal = openMovieDetailsModal;
window.closeModal = closeModal;
window.API = API;

// Format Currency
function formatCurrency(amount) {
    return `Rs. ${Number(amount || 0).toFixed(2)}`;
}

// Toast Notifications
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? '✓' : type === 'error' ? '⚠' : 'ℹ';
    toast.innerHTML = `<span style="font-weight:bold;">${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 350);
    }, 4000);
}

// Global Auth Notice Modal (Explaining Phase 2 roadmap while enabling seamless guest booking)
function openAuthModal(type = 'login') {
    let modal = document.getElementById('authNoticeModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'authNoticeModal';
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-card">
                <button class="modal-close-btn" onclick="closeModal('authNoticeModal')">&times;</button>
                <div style="text-align:center;">
                    <div style="font-size:2.8rem; margin-bottom:0.75rem;">👑</div>
                    <h3 style="font-family:var(--font-serif); font-size:1.6rem; color:var(--accent-gold); margin-bottom:0.5rem;">
                        CineBook VIP Club
                    </h3>
                    <p style="color:var(--text-secondary); line-height:1.6; margin-bottom:1.5rem;">
                        Authentication and User Profiles are scheduled for <strong>Phase 2</strong>. In this phase, guest checkout is fully enabled so you can reserve seats immediately!
                    </p>
                    <div style="background:var(--bg-surface); padding:1.2rem; border-radius:var(--radius-md); text-align:left; border:1px solid var(--border-subtle); margin-bottom:1.5rem;">
                        <div style="font-weight:700; color:var(--text-primary); margin-bottom:0.4rem;">Coming in Phase 2:</div>
                        <ul style="color:var(--text-muted); font-size:0.88rem; padding-left:1.2rem; line-height:1.7;">
                            <li>Secure User Accounts with phone &amp; email OTP</li>
                            <li>Personalized booking history &amp; one-click reorder</li>
                            <li>Exclusive premier discounts &amp; reward points</li>
                        </ul>
                    </div>
                    <button class="btn-primary" style="width:100%; justify-content:center; padding:0.85rem;" onclick="closeModal('authNoticeModal')">
                        Continue as Guest &rarr;
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }
    setTimeout(() => modal.classList.add('open'), 10);
}

// Movie Details Modal
function openMovieDetailsModal(movie, shows = []) {
    const meta = window.getMovieMeta(movie.movieName);

    let showsHtml = '';
    if (shows.length > 0) {
        showsHtml = shows.map(s => `
            <a href="seats.html?showId=${s.showId}" class="show-item-pill">
                <div>
                    <div class="show-item-time">🕒 ${s.showTime}</div>
                    <div class="show-item-meta">📅 ${s.showDate} &bull; ${s.screenName || 'Screen 1'}</div>
                </div>
                <span class="show-btn-book">Select Seats &rarr;</span>
            </a>
        `).join('');
    } else {
        showsHtml = '<p style="color:var(--text-muted); padding:1rem; text-align:center;">No scheduled shows available today.</p>';
    }

    let modal = document.getElementById('movieDetailsModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'movieDetailsModal';
        modal.className = 'modal-overlay';
        document.body.appendChild(modal);
    }

    modal.innerHTML = `
        <div class="modal-card" style="max-width: 680px;">
            <button class="modal-close-btn" onclick="closeModal('movieDetailsModal')">&times;</button>
            <div style="display:flex; gap:1.5rem; margin-bottom:1.5rem; flex-wrap:wrap;">
                <div style="flex:1; min-width:260px;">
                    <div style="color:var(--accent-purple-light); font-size:0.8rem; font-weight:700; text-transform:uppercase; letter-spacing:1px; margin-bottom:0.25rem;">
                        ${movie.genre} &bull; ${meta.ageCert}
                    </div>
                    <h2 style="font-family:var(--font-serif); font-size:1.8rem; color:var(--text-primary); margin-bottom:0.5rem; line-height:1.2;">
                        ${movie.movieName}
                    </h2>
                    <div style="display:flex; align-items:center; gap:0.9rem; color:var(--text-muted); font-size:0.88rem; margin-bottom:1rem;">
                        <span style="color:var(--accent-gold); font-weight:700;">★ ${meta.rating}</span>
                        <span>⏱ ${movie.duration} mins</span>
                        <span>🌐 ${movie.language}</span>
                    </div>
                    <p style="color:var(--text-secondary); font-size:0.92rem; line-height:1.6; margin-bottom:1rem;">
                        ${meta.synopsis}
                    </p>
                    <div style="font-size:0.85rem; color:var(--text-muted); line-height:1.5;">
                        <div><strong style="color:var(--text-secondary);">Director:</strong> ${meta.director}</div>
                        <div><strong style="color:var(--text-secondary);">Cast:</strong> ${meta.cast}</div>
                    </div>
                </div>
            </div>
            
            <div style="border-top:1px solid var(--border-subtle); padding-top:1.2rem;">
                <h4 style="font-size:0.88rem; font-weight:700; text-transform:uppercase; letter-spacing:1px; color:var(--text-primary); margin-bottom:0.8rem;">
                    Available Screenings &amp; Showtime Booking
                </h4>
                <div style="display:flex; flex-direction:column; gap:0.6rem; max-height:220px; overflow-y:auto; padding-right:0.3rem;">
                    ${showsHtml}
                </div>
            </div>
        </div>
    `;

    setTimeout(() => modal.classList.add('open'), 10);
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('open');
    }
}
