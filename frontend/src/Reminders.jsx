
import { useCallback, useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function Reminders({ token }) {
  const [appointments, setAppointments] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadReminders = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const response = await fetch(`${API_URL}/api/appointments`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      if (!response.ok) {
        throw new Error(`Unable to load reminders. HTTP ${response.status}`)
      }

      const data = await response.json()
      setAppointments(data)

    } catch (err) {
      setError(err.message || 'Unable to load reminders.')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    loadReminders()
  }, [loadReminders])

  const filteredAppointments = appointments.filter(appointment => {
    if (filter === 'SENT') {
      return appointment.reminderSent === true
    }

    if (filter === 'NOT_SENT') {
      return appointment.reminderSent === false
    }

    return true
  })

  const sentCount = appointments.filter(
    appointment => appointment.reminderSent === true
  ).length

  const notSentCount = appointments.filter(
    appointment => appointment.reminderSent === false
  ).length

  return (
    <section className="reminders-page">

      <div className="section-heading">
        <div>
          <h2>Appointment Reminders</h2>
          <p>Track customer appointment reminder emails</p>
        </div>

        <button onClick={loadReminders}>
          Refresh
        </button>
      </div>

      <div className="reminder-stats">

        <div className="reminder-stat-card">
          <p>Total Appointments</p>
          <h2>{appointments.length}</h2>
        </div>

        <div className="reminder-stat-card">
          <p>Reminders Sent</p>
          <h2>{sentCount}</h2>
        </div>

        <div className="reminder-stat-card">
          <p>Reminders Not Sent</p>
          <h2>{notSentCount}</h2>
        </div>

      </div>

      <div className="reminder-filters">

        <button
          className={filter === 'ALL' ? 'selected-filter' : ''}
          onClick={() => setFilter('ALL')}
        >
          All
        </button>

        <button
          className={filter === 'SENT' ? 'selected-filter' : ''}
          onClick={() => setFilter('SENT')}
        >
          Sent
        </button>

        <button
          className={filter === 'NOT_SENT' ? 'selected-filter' : ''}
          onClick={() => setFilter('NOT_SENT')}
        >
          Not Sent
        </button>

      </div>

      {error && (
        <p className="login-error">{error}</p>
      )}

      {loading ? (
        <p>Loading reminder records...</p>
      ) : filteredAppointments.length === 0 ? (

        <div className="empty-state">
          <h3>No reminder records found</h3>
          <p>There are no appointments in this filter.</p>
        </div>

      ) : (

        <div className="reminder-list">

          {filteredAppointments.map(appointment => (

            <div className="reminder-card" key={appointment.id}>

              <div className="reminder-info">
                <h3>{appointment.customerName}</h3>

                <p>{appointment.customerEmail}</p>

                <p>Service: {appointment.serviceName}</p>

                <p>
                  Appointment:{' '}
                  {new Date(
                    appointment.appointmentDateTime
                  ).toLocaleString()}
                </p>

                <p>Appointment status: {appointment.status}</p>
              </div>

              <div className="reminder-status">

                <span className={
                  appointment.reminderSent
                    ? 'reminder-sent'
                    : 'reminder-not-sent'
                }>
                  {appointment.reminderSent
                    ? 'REMINDER SENT'
                    : 'NOT SENT'}
                </span>

                <small>
                  {appointment.reminderSent
                    ? 'Confirmation of reminder email recorded'
                    : 'No reminder email recorded yet'}
                </small>

              </div>

            </div>

          ))}

        </div>

      )}

    </section>
  )
}

export default Reminders
