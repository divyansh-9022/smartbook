import { useCallback, useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

const DAYS = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY'
]

function BusinessHours({ token }) {
  const [hours, setHours] = useState([])
  const [breaks, setBreaks] = useState([])

  const [loading, setLoading] = useState(true)
  const [savingDay, setSavingDay] = useState(null)
  const [savingBreak, setSavingBreak] = useState(false)

  const [breakDay, setBreakDay] = useState('MONDAY')
  const [breakStart, setBreakStart] = useState('13:00')
  const [breakEnd, setBreakEnd] = useState('14:00')
  const [breakName, setBreakName] = useState('Lunch Break')

  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadHours = useCallback(async () => {
    const response = await fetch(`${API_URL}/api/business-hours`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error('Unable to load business hours.')
    }

    const data = await response.json()

    setHours(data.map(day => ({
      ...day,
      openTime: day.openTime ? day.openTime.slice(0, 5) : '09:00',
      closeTime: day.closeTime ? day.closeTime.slice(0, 5) : '18:00'
    })))
  }, [token])

  const loadBreaks = useCallback(async () => {
    const response = await fetch(`${API_URL}/api/business-breaks`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error('Unable to load business breaks.')
    }

    const data = await response.json()
    setBreaks(data)
  }, [token])

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      await Promise.all([loadHours(), loadBreaks()])
    } catch (err) {
      setError(err.message || 'Unable to load settings.')
    } finally {
      setLoading(false)
    }
  }, [loadHours, loadBreaks])

  useEffect(() => {
    loadAll()
  }, [loadAll])

  const updateDay = (dayOfWeek, field, value) => {
    setHours(previous =>
      previous.map(day => {
        if (day.dayOfWeek !== dayOfWeek) {
          return day
        }

        return {
          ...day,
          [field]: value
        }
      })
    )
  }

  const saveDay = async (day) => {
    setSavingDay(day.dayOfWeek)
    setError('')
    setMessage('')

    try {
      const response = await fetch(`${API_URL}/api/business-hours`, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          dayOfWeek: day.dayOfWeek,
          openTime: day.closed ? null : `${day.openTime}:00`,
          closeTime: day.closed ? null : `${day.closeTime}:00`,
          closed: day.closed
        })
      })

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(data.message || 'Unable to save business hours.')
      }

      setMessage(`${day.dayOfWeek} hours saved successfully.`)
      await loadHours()

    } catch (err) {
      setError(err.message || 'Unable to save business hours.')
    } finally {
      setSavingDay(null)
    }
  }

  const addBreak = async (event) => {
    event.preventDefault()

    setError('')
    setMessage('')

    if (!breakName.trim()) {
      setError('Please enter a break name.')
      return
    }

    if (breakStart >= breakEnd) {
      setError('Break end time must be after start time.')
      return
    }

    setSavingBreak(true)

    try {
      const response = await fetch(`${API_URL}/api/business-breaks`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          dayOfWeek: breakDay,
          startTime: `${breakStart}:00`,
          endTime: `${breakEnd}:00`,
          breakName: breakName.trim()
        })
      })

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(
          data.message || data.detail || 'Unable to add break.'
        )
      }

      setMessage(`${breakName} added successfully.`)
      await loadBreaks()

    } catch (err) {
      setError(err.message || 'Unable to add break.')
    } finally {
      setSavingBreak(false)
    }
  }

  const deleteBreak = async (id) => {
    setError('')
    setMessage('')

    try {
      const response = await fetch(
        `${API_URL}/api/business-breaks/${id}`,
        {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        throw new Error('Unable to delete break.')
      }

      setMessage('Break deleted successfully.')
      await loadBreaks()

    } catch (err) {
      setError(err.message || 'Unable to delete break.')
    }
  }

  if (loading) {
    return <p>Loading business settings...</p>
  }

  return (
    <section className="business-hours-page">

      <div className="section-heading">
        <div>
          <h2>Business Hours & Breaks</h2>
          <p>Manage your weekly opening hours and scheduled breaks.</p>
        </div>

        <button onClick={loadAll}>
          Refresh
        </button>
      </div>

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      <div className="hours-info">
        Changes to business hours and breaks affect customer appointment
        bookings. Appointments during closed hours or scheduled breaks
        will not be available.
      </div>

      <h3>Weekly Business Hours</h3>

      <div className="hours-list">

        {hours.map(day => (
          <div className="hours-card" key={day.dayOfWeek}>

            <div className="hours-day">
              <h3>{day.dayOfWeek}</h3>
            </div>

            <label className="closed-toggle">
              <input
                type="checkbox"
                checked={day.closed}
                onChange={event => {
                  const closed = event.target.checked

                  setHours(previous =>
                    previous.map(item =>
                      item.dayOfWeek === day.dayOfWeek
                        ? {
                            ...item,
                            closed,
                            openTime: item.openTime || '09:00',
                            closeTime: item.closeTime || '18:00'
                          }
                        : item
                    )
                  )
                }}
              />
              Closed
            </label>

            {!day.closed && (
              <div className="hours-time-fields">

                <div>
                  <label>Opening time</label>
                  <input
                    type="time"
                    value={day.openTime}
                    onChange={event =>
                      updateDay(
                        day.dayOfWeek,
                        'openTime',
                        event.target.value
                      )
                    }
                  />
                </div>

                <div>
                  <label>Closing time</label>
                  <input
                    type="time"
                    value={day.closeTime}
                    onChange={event =>
                      updateDay(
                        day.dayOfWeek,
                        'closeTime',
                        event.target.value
                      )
                    }
                  />
                </div>

              </div>
            )}

            <button
              className="save-hours-button"
              disabled={savingDay === day.dayOfWeek}
              onClick={() => saveDay(day)}
            >
              {savingDay === day.dayOfWeek ? 'Saving...' : 'Save'}
            </button>

          </div>
        ))}

      </div>

      <div className="breaks-section">

        <h3>Scheduled Breaks</h3>

        <p>
          Add lunch breaks, staff breaks or other unavailable time periods.
        </p>

        <form className="break-form" onSubmit={addBreak}>

          <div>
            <label>Break name</label>
            <input
              type="text"
              value={breakName}
              onChange={event => setBreakName(event.target.value)}
              placeholder="e.g. Lunch Break"
              maxLength={100}
              required
            />
          </div>

          <div>
            <label>Day</label>
            <select
              value={breakDay}
              onChange={event => setBreakDay(event.target.value)}
            >
              {DAYS.map(day => (
                <option key={day} value={day}>
                  {day}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label>Start time</label>
            <input
              type="time"
              value={breakStart}
              onChange={event => setBreakStart(event.target.value)}
              required
            />
          </div>

          <div>
            <label>End time</label>
            <input
              type="time"
              value={breakEnd}
              onChange={event => setBreakEnd(event.target.value)}
              required
            />
          </div>

          <button type="submit" disabled={savingBreak}>
            {savingBreak ? 'Adding...' : 'Add Break'}
          </button>

        </form>

        <h3>Existing Breaks</h3>

        {breaks.length === 0 ? (
          <p>No scheduled breaks yet.</p>
        ) : (
          <div className="break-list">

            {breaks.map(item => (
              <div className="break-card" key={item.id}>

                <div>
                  <strong>{item.breakName}</strong>
                  <p>
                    {item.dayOfWeek} | {item.startTime.slice(0, 5)}
                    {' – '}
                    {item.endTime.slice(0, 5)}
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => deleteBreak(item.id)}
                >
                  Delete
                </button>

              </div>
            ))}

          </div>
        )}

      </div>

    </section>
  )
}

export default BusinessHours
