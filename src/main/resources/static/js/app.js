/**
 * CineBook 2.0 – Core Frontend Client & Cinema Application Engine
 * Connects directly to Spring Boot REST Controllers & MySQL JDBC backend
 */

const API = {
    getMovies: () => fetch('/api/movies').then(r => {
        if (!r.ok) throw new Error('Failed to load movies catalog');
        return r.json();
    }),
    getShows: () => fetch('/api/shows').then(r => {
        if (!r.ok) throw new Error('Failed to load show schedules');
        return r.json();
    }),
    getShow: (id) => fetch(`/api/shows/${id}`).then(r => {
        if (!r.ok) throw new Error('Show screening not found');
        return r.json();
    }),
    getSeatMap: (showId) => fetch(`/api/shows/${showId}/seats`).then(r => {
        if (!r.ok) throw new Error('Failed to fetch seat inventory');
        return r.json();
    }),
    bookTickets: (data) => fetch('/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    }).then(async r => {
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Failed to complete ticket booking');
        return json;
    }),
    getBooking: (id) => fetch(`/api/bookings/${id}`).then(async r => {
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Booking record not found');
        return json;
    }),
    getCustomerBookings: (phone) => fetch(`/api/bookings/customer/${phone}`).then(r => {
        if (!r.ok) throw new Error('Failed to search customer bookings');
        return r.json();
    }),
    cancelBooking: (id) => fetch(`/api/bookings/${id}/cancel`, {
        method: 'POST'
    }).then(async r => {
        const json = await r.json();
        if (!r.ok) throw new Error(json.message || 'Failed to process cancellation and refund');
        return json;
    })
};

// Rich Cinematic Movie Metadata (Maps seamlessly to real backend DB records)
const MovieMetadata = {
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
        trailerUrl: 'https://www.youtube.com/watch?v=YoHD9XEInc0'
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
        trailerUrl: 'https://www.youtube.com/watch?v=zSWdZVtXT7E'
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
        trailerUrl: 'https://www.youtube.com/watch?v=EXeTwQWrcwY'
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
        trailerUrl: 'https://www.youtube.com/watch?v=uYPbbksJxIg'
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
        trailerUrl: 'https://www.youtube.com/watch?v=d9MyW72ELq0'
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
        trailerUrl: 'https://www.youtube.com/watch?v=4rgYUipGJNo'
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
        trailerUrl: 'https://www.youtube.com/watch?v=cqGjhVJWtEg'
    }
};

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
    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 350);
    }, 4200);
}

// Global Modal Management
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
                        Authentication and Customer Profiles are scheduled for <strong>Phase 2</strong>. In this phase, guest checkout is fully enabled so you can reserve seats immediately!
                    </p>
                    <div style="background:var(--bg-surface); padding:1.2rem; border-radius:var(--radius-md); text-align:left; border:1px solid var(--border-subtle); margin-bottom:1.5rem;">
                        <div style="font-weight:700; color:var(--text-primary); margin-bottom:0.4rem;">Coming in Phase 2:</div>
                        <ul style="color:var(--text-muted); font-size:0.88rem; padding-left:1.2rem; line-height:1.7;">
                            <li>Secure User Accounts with phone &amp; email OTP</li>
                            <li>Personalized booking history &amp; one-click reorder</li>
                            <li>Exclusive premier discounts &amp; reward points</li>
                        </ul>
                    </div>
                    <button class="btn-hero-primary" style="width:100%; justify-content:center;" onclick="closeModal('authNoticeModal')">
                        Continue as Guest &rarr;
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }
    setTimeout(() => modal.classList.add('open'), 10);
}

function openMovieDetailsModal(movie, shows = []) {
    const meta = MovieMetadata[movie.movieName] || {
        rating: '8.5',
        votes: '500K',
        director: 'Acclaimed Filmmaker',
        cast: 'Stellar Ensemble Cast',
        tagline: 'An unforgettable cinematic experience.',
        synopsis: 'Experience this masterpiece in ultra-high-definition sound and projection at CineBook.',
        ageCert: 'UA'
    };

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
