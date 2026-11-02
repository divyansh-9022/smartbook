import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function Analytics({ token }) {
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadAnalytics = async () => {
      try {
        const response = await fetch(`${API_URL}/api/appointments`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        })

        if (!response.ok) {
          throw new Error('Unable to load analytics data.')
        }

        const data = await response.json()
        setAppointments(data)

      } catch (err) {
        setError(err.message || 'Unable to load analytics.')
      } finally {
        setLoading(false)
      }
    }

    loadAnalytics()
  }, [token])

  if (loading) {
    return <p>Loading analytics...</p>
  }

  if (error) {
    return <p className="login-error">{error}</p>
  }

  const bookedAppointments = appointments.filter(
    appointment => appointment.status === 'BOOKED'
  )

  const cancelledAppointments = appointments.filter(
    appointment => appointment.status === 'CANCELLED'
  )

  const totalBookedValue = bookedAppointments.reduce(
    (total, appointment) => total + Number(appointment.price || 0),
    0
  )

  const uniqueCustomers = new Set(
    appointments.map(appointment => appointment.customerEmail)
  )

  const serviceCounts = {}

  appointments.forEach(appointment => {
    const name = appointment.serviceName

    serviceCounts[name] = (serviceCounts[name] || 0) + 1
  })

  const popularServices = Object.entries(serviceCounts)
    .sort((a, b) => b[1] - a[1])

  const monthlyData = Array.from({ length: 6 }, (_, index) => {
    const date = new Date()
    date.setDate(1)
    date.setMonth(date.getMonth() - (5 - index))

    const year = date.getFullYear()
    const month = date.getMonth()

    const count = appointments.filter(appointment => {
      const appointmentDate = new Date(appointment.appointmentDateTime)

      return (
        appointmentDate.getFullYear() === year &&
        appointmentDate.getMonth() === month
      )
    }).length

    return {
      label: date.toLocaleDateString('en-IN', { month: 'short' }),
      count
    }
  })

  const maxMonthlyCount = Math.max(
    ...monthlyData.map(item => item.count),
    1
  )

  return (
    <section className="analytics-page">

      <div className="section-heading">
        <div>
          <h2>Business Analytics</h2>
          <p>Understand your business performance</p>
        </div>
      </div>

      <div className="analytics-stats">

        <div className="analytics-card">
          <p>Total Appointments</p>
          <h2>{appointments.length}</h2>
        </div>

        <div className="analytics-card">
          <p>Booked Appointments</p>
          <h2>{bookedAppointments.length}</h2>
        </div>

        <div className="analytics-card">
          <p>Total Customers</p>
          <h2>{uniqueCustomers.size}</h2>
        </div>

        <div className="analytics-card">
          <p>Cancelled Appointments</p>
          <h2>{cancelledAppointments.length}</h2>
        </div>

        <div className="analytics-card">
          <p>Estimated Booked Value</p>
          <h2>
            ₹{totalBookedValue.toLocaleString('en-IN')}
          </h2>
        </div>

      </div>

      <div className="analytics-panel">

        <h3>Appointment Trends</h3>
        <p>Appointments during the last six months</p>

        <div className="monthly-chart">

          {monthlyData.map((item, index) => (
            <div className="chart-column" key={index}>

              <span className="chart-value">{item.count}</span>

              <div className="chart-bar-container">
                <div
                  className="chart-bar"
                  style={{
                    height: `${(item.count / maxMonthlyCount) * 100}%`
                  }}
                />
              </div>

              <span className="chart-label">{item.label}</span>

            </div>
          ))}

        </div>

      </div>

      <div className="analytics-panel">

        <h3>Popular Services</h3>

        {popularServices.length === 0 ? (
          <p>No service bookings available yet.</p>
        ) : (
          <div className="popular-services">

            {popularServices.map(([name, count]) => (
              <div className="popular-service" key={name}>
                <span>{name}</span>
                <strong>{count} bookings</strong>
              </div>
            ))}

          </div>
        )}

      </div>

      <p className="analytics-note">
        Estimated booked value is calculated from active booked appointments.
        It does not represent confirmed payments or collected revenue.
      </p>

    </section>
  )
}

export default Analytics