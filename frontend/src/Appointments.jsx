
import { useCallback, useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function Appointments({ token }) {
  const [appointments, setAppointments] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [reschedulingId, setReschedulingId] = useState(null)
  const [newDateTime, setNewDateTime] = useState('')
  const [saving, setSaving] = useState(false)
  const [calendarDate, setCalendarDate] = useState(new Date())
  const [calendarView, setCalendarView] = useState('month')
  const [showCalendar, setShowCalendar] = useState(true)

  const loadAppointments = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const response = await fetch(`${API_URL}/api/appointments`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      if (!response.ok) {
        throw new Error(`Unable to load appointments. HTTP ${response.status}`)
      }

      const data = await response.json()
      setAppointments(data)

    } catch (err) {
      setError(err.message || 'Unable to load appointments.')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    loadAppointments()
  }, [loadAppointments])

  const cancelAppointment = async (id) => {
    const confirmed = window.confirm(
      'Are you sure you want to cancel this appointment?'
    )

    if (!confirmed) return

    setError('')
    setMessage('')

    try {
      const response = await fetch(
        `${API_URL}/api/appointments/${id}/cancel`,
        {
          method: 'PATCH',
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        throw new Error(`Cancellation failed. HTTP ${response.status}`)
      }

      setMessage('Appointment cancelled successfully.')
      await loadAppointments()

    } catch (err) {
      setError(err.message || 'Unable to cancel appointment.')
    }
  }

  const rescheduleAppointment = async (id) => {
    if (!newDateTime) {
      setError('Please select a new appointment date and time.')
      return
    }

    setSaving(true)
    setError('')
    setMessage('')

    try {
      const response = await fetch(
        `${API_URL}/api/appointments/${id}/reschedule`,
        {
          method: 'PATCH',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            appointmentDateTime: newDateTime
          })
        }
      )

      if (!response.ok) {
        const responseText = await response.text()
        let errorMessage = `Rescheduling failed. HTTP ${response.status}`

        if (responseText) {
          try {
            const data = JSON.parse(responseText)
            errorMessage = data.message || errorMessage
          } catch {
            errorMessage = responseText
          }
        }

        throw new Error(errorMessage)
      }

      setMessage('Appointment rescheduled successfully.')
      setReschedulingId(null)
      setNewDateTime('')
      await loadAppointments()

    } catch (err) {
      setError(err.message || 'Unable to reschedule appointment.')
    } finally {
      setSaving(false)
    }
  }

  const filteredAppointments = appointments.filter((appointment) => {
    const searchText = search.toLowerCase()

    return (
      appointment.customerName.toLowerCase().includes(searchText) ||
      appointment.customerEmail.toLowerCase().includes(searchText) ||
      appointment.serviceName.toLowerCase().includes(searchText)
    )
  })
const getDateKey = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const getAppointmentsForDate = (date) => {
  const dateKey = getDateKey(date)

  return filteredAppointments.filter(appointment =>
    appointment.appointmentDateTime?.slice(0, 10) === dateKey
  )
}

const calendarDays = (() => {
  if (calendarView === 'day') {
    return [new Date(calendarDate)]
  }

  const start = new Date(calendarDate)

  if (calendarView === 'month') {
    start.setDate(1)
  }

  start.setDate(start.getDate() - start.getDay())

  const count = calendarView === 'week' ? 7 : 42

  return Array.from({ length: count }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return date
  })
})()

const calendarTitle = calendarView === 'month'
  ? calendarDate.toLocaleDateString('en-IN', {
      month: 'long',
      year: 'numeric'
    })
  : calendarView === 'week'
    ? `${calendarDays[0].toLocaleDateString('en-IN')} – ${calendarDays[6].toLocaleDateString('en-IN')}`
    : calendarDate.toLocaleDateString('en-IN', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      })

const changeCalendarDate = (direction) => {
  setCalendarDate(current => {
    const next = new Date(current)

    if (calendarView === 'month') {
      next.setMonth(next.getMonth() + direction)
    } else if (calendarView === 'week') {
      next.setDate(next.getDate() + direction * 7)
    } else {
      next.setDate(next.getDate() + direction)
    }

    return next
  })
}

  return (
    <section className="appointments-page">

      <div className="section-heading">
        <div>
          <h2>Appointments</h2>
          <p>Manage your customer bookings</p>
        </div>

        <button onClick={loadAppointments}>
          Refresh
        </button>
      </div>

      <input
        type="search"
        placeholder="Search by customer, email or service..."
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        className="appointment-search"
      />
      <div className="calendar-section">

        <div className="calendar-heading">
          <h3>Appointment Calendar</h3>

          <button
            type="button"
            onClick={() => setShowCalendar(!showCalendar)}
          >
            {showCalendar ? 'Hide Calendar' : 'Show Calendar'}
          </button>
        </div>

        {showCalendar && (
            <>
          <div className="calendar-controls">

            <button type="button" onClick={() => changeCalendarDate(-1)}>
              Previous
            </button>

            <strong>{calendarTitle}</strong>

            <button type="button" onClick={() => changeCalendarDate(1)}>
              Next
            </button>

            <button
              type="button"
              onClick={() => setCalendarDate(new Date())}
            >
              Today
            </button>

            <select
              value={calendarView}
              onChange={(event) => setCalendarView(event.target.value)}
            >
              <option value="month">Month</option>
              <option value="week">Week</option>
              <option value="day">Day</option>
                  </select>
                  </div>

                  <div className={`calendar-grid ${calendarView}-view`}>

                    {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                      <div className="calendar-weekday" key={day}>
                        {day}
                      </div>
                    ))}

                    {calendarDays.map((date, index) => {
                      const dayAppointments = getAppointmentsForDate(date)

                      const isCurrentMonth =
                        date.getMonth() === calendarDate.getMonth()

                      const isToday =
                        getDateKey(date) === getDateKey(new Date())

                      return (
                        <div
                          key={getDateKey(date)}
                          className={`calendar-day
                            ${isCurrentMonth ? '' : 'other-month'}
                            ${isToday ? 'today' : ''}`}
                          onClick={() => {
                            setCalendarDate(new Date(date))
                            setCalendarView('day')
                          }}
                        >
                          <span className="calendar-day-number">
                            {date.getDate()}
                          </span>

                          <div className="calendar-day-appointments">

                            {dayAppointments.map(appointment => (
                              <div
                                key={appointment.id}
                                className={`calendar-event ${
                                  appointment.status === 'BOOKED'
                                    ? 'event-booked'
                                    : 'event-cancelled'
                                }`}
                                title={`${appointment.customerName} - ${appointment.serviceName}`}
                              >
                                {new Date(
                                  appointment.appointmentDateTime
                                ).toLocaleTimeString([], {
                                  hour: '2-digit',
                                  minute: '2-digit'
                                })}

                                {' - '}

                                {appointment.customerName}
                              </div>
                            ))}

                          </div>
                        </div>
                      )
                    })}




                </div>
                </>
              )}


      </div>

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      {loading ? (
        <p>Loading appointments...</p>
      ) : filteredAppointments.length === 0 ? (
        <div className="empty-state">
          <h3>No appointments found</h3>
          <p>Try another search or check your bookings.</p>
        </div>
      ) : (

        <div className="appointment-list">

          {filteredAppointments.map((appointment) => (
            <div className="appointment-card" key={appointment.id}>

              <div className="appointment-info">
                <h3>{appointment.customerName}</h3>

                <p>{appointment.customerEmail}</p>
                <p>Service: {appointment.serviceName}</p>
                <p>
                  Date: {new Date(
                    appointment.appointmentDateTime
                  ).toLocaleString()}
                </p>
                <p>
                  Duration: {appointment.durationMinutes} minutes
                </p>
                <p>Price: ₹{appointment.price}</p>

                <strong className={
                  appointment.status === 'BOOKED'
                    ? 'status-booked'
                    : 'status-cancelled'
                }>
                  {appointment.status}
                </strong>
              </div>

              {appointment.status === 'BOOKED' && (
                <div className="appointment-actions">

                  <button
                    onClick={() => {
                      setReschedulingId(
                        reschedulingId === appointment.id
                          ? null
                          : appointment.id
                      )
                      setNewDateTime('')
                      setError('')
                    }}
                  >
                    Reschedule
                  </button>

                  <button
                    className="cancel-button"
                    onClick={() => cancelAppointment(appointment.id)}
                  >
                    Cancel
                  </button>

                </div>
              )}

              {reschedulingId === appointment.id && (
                <div className="reschedule-form">
                  <label>New appointment date and time</label>

                  <input
                    type="datetime-local"
                    value={newDateTime}
                    onChange={(event) => setNewDateTime(event.target.value)}
                  />

                  <button
                    disabled={saving}
                    onClick={() => rescheduleAppointment(appointment.id)}
                  >
                    {saving ? 'Saving...' : 'Confirm Reschedule'}
                  </button>
                </div>
              )}

            </div>
          ))}

        </div>
      )}

    </section>
  )
}

export default Appointments
