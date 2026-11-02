
import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function getLocalDateString() {
  const now = new Date()
  const localTime = new Date(
    now.getTime() - now.getTimezoneOffset() * 60000
  )
  return localTime.toISOString().slice(0, 10)
}

function BookAppointment({ token, onBooked }) {
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const [form, setForm] = useState({
    serviceId: '',
    customerName: '',
    customerEmail: '',
    customerPhone: ''
  })

  const [appointmentDate, setAppointmentDate] = useState('')

  const [businessId, setBusinessId] = useState(null)
  const [availableSlots, setAvailableSlots] = useState([])
  const [selectedSlot, setSelectedSlot] = useState('')
  const [slotsLoading, setSlotsLoading] = useState(false)

  useEffect(() => {
    const loadServices = async () => {
      try {
        const response = await fetch(`${API_URL}/api/services`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        })

        if (!response.ok) {
          throw new Error('Unable to load services.')
        }

        const data = await response.json()
        setServices(data.filter(service => service.active))

      } catch (err) {
        setError(err.message || 'Unable to load services.')
      } finally {
        setLoading(false)
      }
    }

    loadServices()
  }, [token])
useEffect(() => {
  const loadBusiness = async () => {
    try {
      const response = await fetch(`${API_URL}/api/business-owners/me`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      if (!response.ok) {
        throw new Error('Unable to load business information.')
      }

      const data = await response.json()
      setBusinessId(data.id)

    } catch (err) {
      setError(err.message || 'Unable to load business information.')
    }
  }

  loadBusiness()
}, [token])
useEffect(() => {
  if (!businessId || !form.serviceId || !appointmentDate) {
    setAvailableSlots([])
    setSelectedSlot('')
    return
  }

  const controller = new AbortController()

  const loadAvailableSlots = async () => {
    setSlotsLoading(true)
    setAvailableSlots([])
    setSelectedSlot('')

    try {
      const url = `${API_URL}/api/public/businesses/${businessId}/available-slots?serviceId=${encodeURIComponent(form.serviceId)}&date=${encodeURIComponent(appointmentDate)}`

      const response = await fetch(url, {
        signal: controller.signal
      })

      if (!response.ok) {
        throw new Error('Unable to load available appointment times.')
      }

      const data = await response.json()
      setAvailableSlots(data)

    } catch (err) {
      if (err.name !== 'AbortError') {
        setError(err.message || 'Unable to load available times.')
      }
    } finally {
      if (!controller.signal.aborted) {
        setSlotsLoading(false)
      }
    }
  }

  loadAvailableSlots()

  return () => controller.abort()
}, [businessId, form.serviceId, appointmentDate])

  const handleChange = (event) => {
    const { name, value } = event.target

    setForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    setSaving(true)
    setError('')
    setMessage('')

    if (!appointmentDate) {
      setError('Please select an appointment date.')
      setSaving(false)
      return
    }

    // Convert 12-hour time into 24-hour time for the backend.
    if (!selectedSlot) {
      setError('Please select an available appointment time.')
      setSaving(false)
      return
    }

    const appointmentDateTime = selectedSlot

    try {
      const response = await fetch(`${API_URL}/api/appointments`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...form,
          serviceId: Number(form.serviceId),
          appointmentDateTime
        })
      })

      const responseText = await response.text()
      let data = {}

      if (responseText) {
        try {
          data = JSON.parse(responseText)
        } catch {
          data = { message: responseText }
        }
      }

      if (!response.ok) {
        throw new Error(
          data.message || `Booking failed. HTTP ${response.status}`
        )
      }

      setMessage(`Appointment #${data.id} booked successfully.`)

      setForm({
        serviceId: '',
        customerName: '',
        customerEmail: '',
        customerPhone: ''
      })

      setAppointmentDate('')
      setSelectedSlot('')

      if (onBooked) {
        onBooked()
      }

    } catch (err) {
      setError(err.message || 'Unable to book appointment.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="booking-page">

      <div className="section-heading">
        <div>
          <h2>Book Appointment</h2>
          <p>Create a new customer appointment</p>
        </div>
      </div>

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      {loading ? (
        <p>Loading available services...</p>
      ) : services.length === 0 ? (
        <div className="empty-state">
          <h3>No active services available</h3>
          <p>Add or activate a service before booking.</p>
        </div>
      ) : (

        <form className="booking-form" onSubmit={handleSubmit}>

          <label>Select Service</label>

          <select
            name="serviceId"
            value={form.serviceId}
            onChange={handleChange}
            required
          >
            <option value="">Choose a service</option>

            {services.map(service => (
              <option key={service.id} value={service.id}>
                {service.serviceName} — ₹{service.price} (
                {service.durationMinutes} minutes)
              </option>
            ))}
          </select>

          <label>Customer Name</label>

          <input
            type="text"
            name="customerName"
            placeholder="Enter customer name"
            value={form.customerName}
            onChange={handleChange}
            required
          />

          <label>Customer Email</label>

          <input
            type="email"
            name="customerEmail"
            placeholder="Enter customer email"
            value={form.customerEmail}
            onChange={handleChange}
            required
          />

          <label>Customer Phone</label>

          <input
            type="tel"
            name="customerPhone"
            placeholder="Enter customer phone"
            value={form.customerPhone}
            onChange={handleChange}
          />

          <label>Appointment Date</label>

          <input
            type="date"
            min={getLocalDateString()}
            value={appointmentDate}
            onChange={(event) => setAppointmentDate(event.target.value)}
            required
          />

          <label>Available Appointment Times</label>

          {!form.serviceId || !appointmentDate ? (
            <p className="slot-message">
              Select a service and date to view available times.
            </p>
          ) : slotsLoading ? (
            <p className="slot-message">Checking available times...</p>
          ) : availableSlots.length === 0 ? (
            <p className="slot-message">
              No available appointments for this date.
            </p>
          ) : (
            <div className="available-slots">
              {availableSlots.map(slot => {
                const timeLabel = new Date(
                  slot.appointmentDateTime
                ).toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                  hour12: true
                })

                return (
                  <button
                    type="button"
                    key={slot.appointmentDateTime}
                    className={
                      selectedSlot === slot.appointmentDateTime
                        ? 'time-slot selected-slot'
                        : 'time-slot'
                    }
                    onClick={() => setSelectedSlot(slot.appointmentDateTime)}
                  >
                    {timeLabel}
                  </button>
                )
              })}
            </div>
          )}

          <button type="submit" disabled={saving}>
            {saving ? 'Booking...' : 'Book Appointment'}
          </button>

        </form>
      )}

    </section>
  )
}

export default BookAppointment
