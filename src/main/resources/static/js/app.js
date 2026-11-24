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
    getMyBookings: async () => {
        const r = await fetch('/api/bookings/my');
        if (!r.ok) throw new Error('Failed to load user reservations');
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
// Uses verified official theatrical posters from TMDB (The Movie Database) CDN
window.MovieMetadata = {
    'Inception': {
        poster: 'https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/s3TBrRGB1iav7gFOCNx3H31MoES.jpg',
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
        poster: 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/xJHokMbljvjADYdit5fK5VQsXEG.jpg',
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
        poster: 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg',
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
        poster: 'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/s3TBrRGB1iav7gFOCNx3H31MoES.jpg',
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
        poster: 'https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/xJHokMbljvjADYdit5fK5VQsXEG.jpg',
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
        poster: 'https://image.tmdb.org/t/p/w500/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg',
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
        poster: 'https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg',
        backdrop: 'https://image.tmdb.org/t/p/w1280/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg',
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


// =========================================================================
// CINEMATIC AMBIENT BACKGROUND ENGINE
// Ultra-lightweight particle canvas, pause-on-hidden, reduced-motion aware
// =========================================================================
function initCinematicAmbient() {
    let canvas = document.getElementById('ambientParticlesCanvas');
    if (!canvas) {
        const container = document.querySelector('.cinematic-ambient-bg, .ambient-cosmos');
        if (container) {
            canvas = document.createElement('canvas');
            canvas.id = 'ambientParticlesCanvas';
            canvas.className = 'ambient-particles-canvas';
            container.appendChild(canvas);
        } else {
            return;
        }
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = 0;
    let height = 0;
    let animationFrameId = null;
    let isPaused = false;

    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }
    resize();
    window.addEventListener('resize', resize, { passive: true });

    // Subtle particles: 35 particles only for maximum performance & elegance
    const count = 35;
    const particles = [];
    for (let i = 0; i < count; i++) {
        particles.push({
            x: Math.random() * width,
            y: Math.random() * height,
            radius: Math.random() * 1.6 + 0.6,
            vx: (Math.random() - 0.5) * 0.28,
            vy: (Math.random() - 0.5) * 0.28,
            alpha: Math.random() * 0.5 + 0.2,
            pulseSpeed: Math.random() * 0.015 + 0.005,
            color: Math.random() > 0.4 ? '167, 139, 250' : '197, 164, 109' // Lavender or Champagne Gold
        });
    }

    function render() {
        if (isPaused) return;

        ctx.clearRect(0, 0, width, height);

        // Draw connections
        for (let i = 0; i < count; i++) {
            const p1 = particles[i];
            for (let j = i + 1; j < count; j++) {
                const p2 = particles[j];
                const dx = p1.x - p2.x;
                const dy = p1.y - p2.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 95) {
                    ctx.beginPath();
                    ctx.moveTo(p1.x, p1.y);
                    ctx.lineTo(p2.x, p2.y);
                    ctx.strokeStyle = `rgba(255, 255, 255, ${0.04 * (1 - dist / 95)})`;
                    ctx.lineWidth = 0.6;
                    ctx.stroke();
                }
            }
        }

        // Draw particles
        for (let i = 0; i < count; i++) {
            const p = particles[i];
            p.x += p.vx;
            p.y += p.vy;

            if (p.x < 0) p.x = width;
            else if (p.x > width) p.x = 0;
            if (p.y < 0) p.y = height;
            else if (p.y > height) p.y = 0;

            p.alpha += Math.sin(Date.now() * p.pulseSpeed) * 0.005;
            const currentAlpha = Math.max(0.12, Math.min(0.7, p.alpha));

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(${p.color}, ${currentAlpha})`;
            ctx.shadowBlur = 6;
            ctx.shadowColor = `rgba(${p.color}, 0.6)`;
            ctx.fill();
        }

        if (!prefersReducedMotion) {
            animationFrameId = requestAnimationFrame(render);
        }
    }

    // Pause when tab is not active
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            isPaused = true;
            if (animationFrameId) cancelAnimationFrame(animationFrameId);
        } else {
            isPaused = false;
            if (!prefersReducedMotion) animationFrameId = requestAnimationFrame(render);
        }
    });

    if (!prefersReducedMotion) {
        animationFrameId = requestAnimationFrame(render);
    } else {
        render(); // Single static frame
    }
}

// =========================================================================
// UNIFIED RESPONSIVE NAVBAR SYSTEM
// Role-aware: Customer vs Admin, consistent height, active indicator, mobile drawer
// =========================================================================
function initUnifiedNavbar(user) {
    const navIsland = document.querySelector('.navbar-glass-island');
    if (!navIsland) return;

    const path = window.location.pathname;
    const isHomePage = path.endsWith('index.html') || path === '/' || path === '';
    const isLookupPage = path.endsWith('lookup.html');
    const isAdminPage = path.endsWith('admin.html');

    const role = user && user.role ? user.role.toUpperCase() : 'CUSTOMER';
    const isAdmin = role === 'ADMIN';
    const displayName = user ? (user.fullName || user.username || 'User') : 'Guest';
    const initial = displayName.charAt(0).toUpperCase();

    // 1. Build Left Brand Group
    let brandHtml = '';
    if (isAdmin) {
        brandHtml = `
            <a href="admin.html" class="nav-brand-group">
                <div class="nav-brand-icon-prism admin-prism">👑</div>
                <div class="nav-brand-titles">
                    <span class="nav-brand-title">CINE<span>BOOK</span></span>
                    <span class="nav-brand-sub admin-sub">ADMIN CONSOLE</span>
                </div>
            </a>
        `;
    } else {
        brandHtml = `
            <a href="index.html" class="nav-brand-group">
                <div class="nav-brand-icon-prism">🎟️</div>
                <div class="nav-brand-titles">
                    <span class="nav-brand-title">CINE<span>BOOK</span></span>
                    <span class="nav-brand-sub">LUXURY CINEMA</span>
                </div>
            </a>
        `;
    }

    // 2. Build Center Menu Links
    let menuLinks = [];
    if (isAdmin) {
        menuLinks = [
            { label: 'Customer View', href: 'index.html', active: isHomePage },
            { label: 'All Reservations', href: 'admin.html', active: isAdminPage },
            { label: 'My Bookings', href: 'lookup.html', active: isLookupPage }
        ];
    } else {
        menuLinks = [
            { label: 'Now Showing', href: 'index.html', active: isHomePage },
            { label: 'Movie Catalog', href: 'index.html#catalogSection', active: false },
            { label: 'My Bookings', href: 'lookup.html', active: isLookupPage },
            { label: 'Cancel & Refund', href: 'lookup.html#cancelSection', active: false }
        ];
    }

    const linksHtml = `
        <div class="nav-menu-links">
            ${menuLinks.map(l => `<a href="${l.href}" class="nav-link-pill ${l.active ? 'active' : ''}">${l.label}</a>`).join('')}
        </div>
    `;

    // 3. Build Right Actions (Account Info & Sign Out & Mobile Toggle)
    const userRoleClass = isAdmin ? 'admin-tag' : '';
    const avatarClass = isAdmin ? 'admin-avatar' : '';
    const actionsHtml = `
        <div class="nav-actions-group">
            <div class="nav-user-pill">
                <div class="nav-user-avatar ${avatarClass}">${initial}</div>
                <span class="nav-user-name">${displayName}</span>
                <span class="nav-role-tag ${userRoleClass}">${role}</span>
            </div>
            <button type="button" class="btn-nav-signout" onclick="handleLogout()" title="Sign out of CineBook">
                <span>✕</span> Sign Out
            </button>
            <button type="button" class="nav-mobile-toggle" id="navMobileToggle" aria-label="Toggle Navigation Menu">
                ☰
            </button>
        </div>
    `;

    // 4. Inject into Island Navbar
    navIsland.innerHTML = brandHtml + linksHtml + actionsHtml;

    // 5. Build or Update Mobile Drawer
    let drawer = document.getElementById('navMobileDrawer');
    if (!drawer) {
        drawer = document.createElement('div');
        drawer.id = 'navMobileDrawer';
        drawer.className = 'nav-mobile-drawer';
        navIsland.parentElement.appendChild(drawer);
    }

    drawer.innerHTML = `
        ${menuLinks.map(l => `<a href="${l.href}" class="nav-link-pill ${l.active ? 'active' : ''}">${l.label}</a>`).join('')}
        <div class="nav-mobile-user-row">
            <div style="display:flex; align-items:center; gap:0.6rem;">
                <div class="nav-user-avatar ${avatarClass}">${initial}</div>
                <div>
                    <div style="font-weight:700; font-size:0.9rem; color:var(--text-primary);">${displayName}</div>
                    <div style="font-size:0.75rem; color:var(--text-muted);">${role}</div>
                </div>
            </div>
            <button type="button" class="btn-nav-signout" onclick="handleLogout()">Sign Out</button>
        </div>
    `;

    // 6. Bind Mobile Toggle
    const toggleBtn = document.getElementById('navMobileToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            drawer.classList.toggle('open');
        });
    }

    // Close drawer when clicking any link inside it
    drawer.querySelectorAll('a').forEach(a => {
        a.addEventListener('click', () => drawer.classList.remove('open'));
    });

    // Close drawer when clicking outside
    document.addEventListener('click', (e) => {
        if (drawer.classList.contains('open') && !drawer.contains(e.target) && e.target !== toggleBtn) {
            drawer.classList.remove('open');
        }
    });
}

// Session and Logout Management
async function checkAuthSession() {
    try {
        const r = await fetch('/api/auth/me');
        if (r.status === 401) {
            window.location.href = '/login.html';
            return;
        }
        const data = await r.json();
        if (data.authenticated) {
            initUnifiedNavbar(data);
        } else {
            // Not authenticated, redirect to login
            if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html')) {
                window.location.href = '/login.html';
            }
        }
    } catch (e) {
        console.warn('Session check warning:', e);
    }
}

async function handleLogout() {
    try {
        const r = await fetch('/api/auth/logout', { method: 'POST' });
        const data = await r.json();
        showToast('Signed out successfully.', 'info');
        setTimeout(() => {
            window.location.href = data.redirectUrl || '/login.html';
        }, 300);
    } catch (err) {
        window.location.href = '/login.html';
    }
}

window.checkAuthSession = checkAuthSession;
window.handleLogout = handleLogout;
window.initCinematicAmbient = initCinematicAmbient;
window.initUnifiedNavbar = initUnifiedNavbar;

// Initialize Ambient Background on every page
document.addEventListener('DOMContentLoaded', () => {
    initCinematicAmbient();
});

// Run auth check automatically on protected pages
if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html')) {
    document.addEventListener('DOMContentLoaded', checkAuthSession);
}
