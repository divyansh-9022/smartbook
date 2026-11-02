import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function getLocalDateString() {
  const now = new Date()
  const localTime = new Date(
    now.getTime() - now.getTimezoneOffset() * 60000
  )
  return localTime.toISOString().slice(0, 10)
}

function Holidays({ token }) {
  const [holidays, setHolidays] = useState([])
  const [holidayDate, setHolidayDate] = useState('')
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadHolidays = async () => {
    try {
      const response = await fetch(`${API_URL}/api/business-holidays`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Unable to load holidays.')
      }

      setHolidays(data)
    } catch (err) {
      setError(err.message || 'Unable to load holidays.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadHolidays()
  }, [token])

  const handleSubmit = async (event) => {
    event.preventDefault()

    setSaving(true)
    setError('')
    setMessage('')

    try {
      const response = await fetch(`${API_URL}/api/business-holidays`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          holidayDate,
          name: name.trim()
        })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Unable to add holiday.')
      }

      setMessage('Holiday added successfully.')
      setHolidayDate('')
      setName('')
      await loadHolidays()

    } catch (err) {
      setError(err.message || 'Unable to add holiday.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id) => {
    setError('')
    setMessage('')

    try {
      const response = await fetch(
        `${API_URL}/api/business-holidays/${id}`,
        {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        const data = await response.json()
        throw new Error(data.message || 'Unable to delete holiday.')
      }

      setHolidays(previous => previous.filter(holiday => holiday.id !== id))
      setMessage('Holiday removed successfully.')

    } catch (err) {
      setError(err.message || 'Unable to delete holiday.')
    }
  }

  return (
    <section className="booking-page">

      <div className="section-heading">
        <div>
          <h2>Business Holidays</h2>
          <p>Manage your business holidays and days off.</p>
        </div>
      </div>

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      <form className="booking-form" onSubmit={handleSubmit}>

        <h3>Add a Holiday</h3>

        <label>Holiday Name</label>

        <input
          type="text"
          placeholder="Example: Diwali Holiday"
          value={name}
          onChange={event => setName(event.target.value)}
          maxLength={100}
          required
        />

        <label>Holiday Date</label>

        <input
          type="date"
          min={getLocalDateString()}
          value={holidayDate}
          onChange={event => setHolidayDate(event.target.value)}
          required
        />

        <button type="submit" disabled={saving}>
          {saving ? 'Adding...' : 'Add Holiday'}
        </button>

      </form>

      <div className="holiday-list">

        <h3>Scheduled Holidays</h3>

        {loading ? (
          <p>Loading holidays...</p>
        ) : holidays.length === 0 ? (
          <div className="empty-state">
            <h3>No holidays scheduled</h3>
            <p>Add a holiday using the form above.</p>
          </div>
        ) : (
          holidays.map(holiday => (
            <div className="holiday-card" key={holiday.id}>
              <div>
                <h4>{holiday.name}</h4>
                <p>
                  {new Date(`${holiday.holidayDate}T00:00:00`)
                    .toLocaleDateString('en-IN', {
                      day: '2-digit',
                      month: 'long',
                      year: 'numeric'
                    })}
                </p>
              </div>

              <button
                type="button"
                className="holiday-delete"
                onClick={() => handleDelete(holiday.id)}
              >
                Remove
              </button>
            </div>
          ))
        )}

      </div>

    </section>
  )
}

export default Holidays