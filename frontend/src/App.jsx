
import Settings from './Settings'
import Reminders from './Reminders'
import BookAppointment from './BookAppointment'
import { useEffect, useState } from 'react'
import Login from './Login'
import Appointments from './Appointments'
import Customers from './Customers'
import Services from './Services'
import './App.css'
import Register from './Register'
import PublicBooking from './PublicBooking'
import ResetPassword from './ResetPassword'
import VerifyEmail from './VerifyEmail'
import BusinessHours from './BusinessHours'
import Holidays from './Holidays'
import Analytics from './Analytics'
import LicenseActivation from './LicenseActivation'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function App() {
  const [token, setToken] = useState(
    () => sessionStorage.getItem('smartbookToken')
  )
  const [authPage, setAuthPage] = useState('login')

  const [activePage, setActivePage] = useState('Dashboard')
  const [refreshKey, setRefreshKey] = useState(0)
  const [appointments, setAppointments] = useState([])
  const [customers, setCustomers] = useState([])
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [licenseStatus, setLicenseStatus] = useState(null)
  const [licenseLoading, setLicenseLoading] = useState(false)
  const [licenseError, setLicenseError] = useState('')

  const handleLogin = (newToken) => {
    setToken(newToken)
  }

  const handleLogout = () => {
    sessionStorage.removeItem('smartbookToken')
    sessionStorage.removeItem('smartbookTokenType')
    setToken(null)
    setActivePage('Dashboard')
  }
useEffect(() => {
  if (!token) {
    setLicenseStatus(null)
    return
  }

  let cancelled = false

  const checkLicenseStatus = async () => {
    setLicenseLoading(true)
    setLicenseError('')

    try {
      const response = await fetch(
        `${API_URL}/api/license/status`,
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        throw new Error('Unable to check SmartBook licence.')
      }

      const data = await response.json()

      if (!cancelled) {
        setLicenseStatus(data)
      }

    } catch (err) {
      if (!cancelled) {
        setLicenseError(err.message)
      }
    } finally {
      if (!cancelled) {
        setLicenseLoading(false)
      }
    }
  }

  checkLicenseStatus()

  return () => {
    cancelled = true
  }
}, [token])

  useEffect(() => {
    if (!token) return

    const fetchDashboardData = async () => {
      setLoading(true)
      setError('')

      try {
        const headers = {
          Authorization: `Bearer ${token}`
        }

        const [appointmentResponse, customerResponse, serviceResponse] =
          await Promise.all([
            fetch(`${API_URL}/api/appointments`, { headers }),
            fetch(`${API_URL}/api/customers`, { headers }),
            fetch(`${API_URL}/api/services`, { headers })
          ])

        if (
          !appointmentResponse.ok ||
          !customerResponse.ok ||
          !serviceResponse.ok
        ) {
          throw new Error('Unable to load dashboard data.')
        }

        const [appointmentData, customerData, serviceData] =
          await Promise.all([
            appointmentResponse.json(),
            customerResponse.json(),
            serviceResponse.json()
          ])

        setAppointments(appointmentData)
        setCustomers(customerData)
        setServices(serviceData)

      } catch (err) {
        setError(err.message || 'Unable to connect to SmartBook.')
      } finally {
        setLoading(false)
      }
    }

    fetchDashboardData()
  }, [token, refreshKey])
if (window.location.pathname === '/verify-email') {
  return <VerifyEmail />
}
if (window.location.pathname === '/reset-password') {
  return <ResetPassword />
}
const bookingMatch = window.location.pathname.match(
  /^\/book\/([a-z0-9-]+)\/?$/i
)

if (bookingMatch) {
  return (
    <PublicBooking
      businessId={(bookingMatch[1])}
    />
  )
}

  if (!token) {
    return authPage === 'register' ? (
      <Register
        onRegistered={() => setAuthPage('login')}
        onShowLogin={() => setAuthPage('login')}
      />
    ) : (
      <Login
        onLogin={handleLogin}
        onShowRegister={() => setAuthPage('register')}
      />
    )
  }
if (licenseLoading) {
  return <p>Checking SmartBook licence...</p>
}

if (licenseError) {
  return (
    <div className="license-required-page">
      <h2>Unable to verify licence</h2>
      <p>{licenseError}</p>
      <button onClick={() => window.location.reload()}>
        Retry
      </button>
    </div>
  )
}

if (licenseStatus?.enforcementEnabled && !licenseStatus.activated) {
  return (
    <div className="license-required-page">
      <h2>Activate SmartBook</h2>
      <p>Please activate your software licence to access the dashboard.</p>
      <LicenseActivation
        token={token}
        onActivated={setLicenseStatus}
      />
      <button onClick={handleLogout}>
        Logout
      </button>
    </div>
  )
}

  const upcomingAppointments = appointments.filter(
    (appointment) =>
      appointment.status === 'BOOKED' &&
      new Date(appointment.appointmentDateTime) >= new Date()
  )

  const recentAppointments = [...appointments]
    .sort(
      (a, b) =>
        new Date(a.appointmentDateTime) -
        new Date(b.appointmentDateTime)
    )
    .slice(0, 5)

  return (
    <div className="app">

      <aside className="sidebar">
        <h2 className="logo">SmartBook</h2>

        <nav>
          <a
            className={activePage === 'Dashboard' ? 'active' : ''}
            onClick={() => setActivePage('Dashboard')}
          >
            Dashboard
          </a>

          <a
            className={activePage === 'Analytics' ? 'active' : ''}
            onClick={() => setActivePage('Analytics')}
          >
            Analytics
          </a>

          <a
            className={activePage === 'Appointments' ? 'active' : ''}
            onClick={() => setActivePage('Appointments')}
          >
            Appointments
          </a>

          <a
            className={activePage === 'Customers' ? 'active' : ''}
            onClick={() => setActivePage('Customers')}
          >
            Customers
          </a>

          <a
            className={activePage === 'Services' ? 'active' : ''}
            onClick={() => setActivePage('Services')}
          >
            Services
          </a>
          <a
            className={activePage === 'Business Hours' ? 'active' : ''}
            onClick={() => setActivePage('Business Hours')}
          >
            Business Hours
          </a>
          <a
            className={activePage === 'Holidays' ? 'active' : ''}
            onClick={() => setActivePage('Holidays')}
          >
            Holidays
          </a>
          <a
            className={activePage === 'Reminders' ? 'active' : ''}
            onClick={() => setActivePage('Reminders')}
          >
            Reminders
          </a>
          <a
            className={activePage === 'Settings' ? 'active' : ''}
            onClick={() => setActivePage('Settings')}
          >
            Settings
          </a>
          <a
            className={activePage === 'Book Appointment' ? 'active' : ''}
            onClick={() => setActivePage('Book Appointment')}
          >
            Book Appointment
          </a>
        </nav>

        <div className="sidebar-footer">
          SmartBook v1.0
        </div>
      </aside>

      <main className="main-content">

        <header className="topbar">
          <div>
            <h2>{activePage}</h2>
            <p>Welcome back to SmartBook!</p>
          </div>

          <div className="profile">
            <span>Business Owner</span>
            <div className="avatar">B</div>

            <button
              className="logout-button"
              onClick={handleLogout}
            >
              Logout
            </button>
          </div>
        </header>

        {activePage === 'Appointments' ? (

          <Appointments token={token} />

        ) : activePage === 'Customers' ? (

          <Customers token={token} />

        ) : activePage === 'Services' ? (

          <Services token={token} />

        ) : activePage === 'Book Appointment' ? (

          <BookAppointment
            token={token}
            onBooked={() => setRefreshKey(previous => previous + 1)}
          />



          ) : activePage === 'Reminders' ? (

            <Reminders token={token} />

          ) : activePage === 'Settings' ? (

            <Settings token={token} />

          ) : activePage === 'Business Hours' ? (

            <BusinessHours token={token} />

          ) : activePage === 'Holidays' ? (

            <Holidays token={token} />

          ) : activePage === 'Analytics' ? (

            <Analytics token={token} />

          ) : (


          <>
            {loading && <p>Loading dashboard data...</p>}

            {error && (
              <p className="login-error">{error}</p>
            )}

            <section className="stats">

              <div className="stat-card">
                <p>Total Appointments</p>
                <h1>{appointments.length}</h1>
              </div>

              <div className="stat-card">
                <p>Total Customers</p>
                <h1>{customers.length}</h1>
              </div>

              <div className="stat-card">
                <p>Active Services</p>
                <h1>
                  {services.filter(service => service.active).length}
                </h1>
              </div>

              <div className="stat-card">
                <p>Upcoming Appointments</p>
                <h1>{upcomingAppointments.length}</h1>
              </div>

            </section>

            <section className="appointments">

              <div className="section-heading">
                <h2>Recent Appointments</h2>

                <button
                  onClick={() => setActivePage('Appointments')}
                >
                  View All
                </button>
              </div>

              {recentAppointments.length === 0 ? (

                <div className="empty-state">
                  <h3>No appointments found</h3>
                  <p>Your appointments will appear here.</p>
                </div>

              ) : (

                <div style={{ overflowX: 'auto' }}>
                  <table style={{
                    width: '100%',
                    borderCollapse: 'collapse',
                    textAlign: 'left'
                  }}>
                    <thead>
                      <tr>
                        <th style={{ padding: '14px' }}>Customer</th>
                        <th style={{ padding: '14px' }}>Service</th>
                        <th style={{ padding: '14px' }}>Date & Time</th>
                        <th style={{ padding: '14px' }}>Status</th>
                      </tr>
                    </thead>

                    <tbody>
                      {recentAppointments.map(appointment => (
                        <tr key={appointment.id}>
                          <td style={{ padding: '14px' }}>
                            {appointment.customerName}
                          </td>

                          <td style={{ padding: '14px' }}>
                            {appointment.serviceName}
                          </td>

                          <td style={{ padding: '14px' }}>
                            {new Date(
                              appointment.appointmentDateTime
                            ).toLocaleString()}
                          </td>

                          <td style={{ padding: '14px' }}>
                            {appointment.status}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

              )}

            </section>
          </>
        )}

      </main>
    </div>
  )
}

export default App
