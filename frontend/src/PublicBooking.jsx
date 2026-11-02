
import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function getLocalDate() {
  const now = new Date()
  const local = new Date(
    now.getTime() - now.getTimezoneOffset() * 60000
  )
  return local.toISOString().slice(0, 10)
}

function PublicBooking({ businessId }) {
  const [services, setServices] = useState([])
  const [business, setBusiness] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [appointmentId, setAppointmentId] = useState(null)

  const [form, setForm] = useState({
    serviceId: '',
    customerName: '',
    customerEmail: '',
    customerPhone: ''
  })

  const [appointmentDate, setAppointmentDate] = useState('')
  const [availableSlots, setAvailableSlots] = useState([])
  const [selectedSlot, setSelectedSlot] = useState('')
  const [slotsLoading, setSlotsLoading] = useState(false)


useEffect(() => {
  const loadServices = async () => {
    try {
      const [businessResponse, servicesResponse] = await Promise.all([
        fetch(`${API_URL}/api/public/businesses/${businessId}`),
        fetch(`${API_URL}/api/public/businesses/${businessId}/services`)
      ])

      if (!businessResponse.ok || !servicesResponse.ok) {
        throw new Error('Unable to load business information.')
      }

      const businessData = await businessResponse.json()
      const servicesData = await servicesResponse.json()

      setBusiness(businessData)
      setServices(servicesData)

    } catch (err) {
      setError(err.message || 'Unable to load services.')
    } finally {
      setLoading(false)
    }
    }

    loadServices()
    }, [businessId])


      useEffect(() => {
  if (!form.serviceId || !appointmentDate) {
    setAvailableSlots([])
    setSelectedSlot('')
    return
  }

  const controller = new AbortController()

  const loadAvailableSlots = async () => {
    setSlotsLoading(true)
    setAvailableSlots([])
    setSelectedSlot('')
    setError('')

    try {
      const url =
        `${API_URL}/api/public/businesses/${businessId}/available-slots` +
        `?serviceId=${encodeURIComponent(form.serviceId)}` +
        `&date=${encodeURIComponent(appointmentDate)}`

      const response = await fetch(url, {
        signal: controller.signal
      })

      if (!response.ok) {
        throw new Error('Unable to load available appointment slots.')
      }

      const data = await response.json()
      setAvailableSlots(data)
    } catch (err) {
      if (err.name !== 'AbortError') {
        setError(err.message || 'Unable to load available slots.')
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

    setError('')
    setMessage('')
    setAppointmentId(null)

    if (!appointmentDate) {
      setError('Please select an appointment date.')
      return
    }

    if (!selectedSlot) {
      setError('Please select an available appointment time.')
      setSaving(false)
      return
    }

    const appointmentDateTime = selectedSlot

    const selectedDateTime = new Date(appointmentDateTime)

    if (selectedDateTime <= new Date()) {
      setError('Please select a future appointment date and time.')
      return
    }

    setSaving(true)

    try {
      const response = await fetch(
        `${API_URL}/api/public/businesses/${businessId}/appointments`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            ...form,
            serviceId: Number(form.serviceId),
            appointmentDateTime
          })
        }
      )

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

      setAppointmentId(data.id)
      setMessage('Your appointment has been booked successfully!')

      setForm({
        serviceId: '',
        customerName: '',
        customerEmail: '',
        customerPhone: ''
      })

      setAppointmentDate('')
      setSelectedSlot('')

    } catch (err) {
      setError(err.message || 'Unable to book appointment.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="public-booking-page">

      <div className="public-booking-card">

        <div className="public-business-branding">

          {business?.logoUrl && (
            <img
              src={`${API_URL}${business.logoUrl}`}
              alt={`${business.businessName} logo`}
              className="public-business-logo"
            />
          )}

          <h1>{business?.businessName || 'SmartBook'}</h1>

        </div>

        <h2>Book Your Appointment</h2>

        <p className="public-booking-subtitle">
          Choose a service and select a convenient time.
        </p>

        {message && (
          <div className="public-booking-success">
            <h3>Booking Confirmed!</h3>
            <p>{message}</p>
            <p>Appointment ID: #{appointmentId}</p>
            <p>Please check your email for confirmation.</p>
          </div>
        )}

        {error && (
          <p className="login-error">{error}</p>
        )}

        {loading ? (
          <p>Loading available services...</p>
        ) : services.length === 0 ? (
          <div className="empty-state">
            <h3>No services currently available</h3>
            <p>Please contact the business for assistance.</p>
          </div>
        ) : (

          <form className="public-booking-form" onSubmit={handleSubmit}>

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

            <label>Your Name</label>

            <input
              type="text"
              name="customerName"
              placeholder="Enter your name"
              value={form.customerName}
              onChange={handleChange}
              required
            />

            <label>Email Address</label>

            <input
              type="email"
              name="customerEmail"
              placeholder="Enter your email"
              value={form.customerEmail}
              onChange={handleChange}
              required
            />

            <label>Phone Number</label>

            <input
              type="tel"
              name="customerPhone"
              placeholder="Enter your phone number"
              value={form.customerPhone}
              onChange={handleChange}
            />

            <label>Appointment Date</label>

            <input
              type="date"
              min={getLocalDate()}
              value={appointmentDate}
              onChange={event => setAppointmentDate(event.target.value)}
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
    {availableSlots.map((slot) => {
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
              {saving ? 'Booking...' : 'Confirm Appointment'}
            </button>

          </form>
        )}

        <p className="public-booking-footer">
          Powered by SmartBook
        </p>

      </div>

    </div>
  )
}

export default PublicBooking
