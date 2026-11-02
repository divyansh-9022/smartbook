
import { useCallback, useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const emptyForm = {
  serviceName: '',
  description: '',
  price: '',
  durationMinutes: ''
}

function Services({ token }) {
  const [services, setServices] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadServices = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const response = await fetch(`${API_URL}/api/services`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      if (!response.ok) {
        throw new Error(`Unable to load services. HTTP ${response.status}`)
      }

      const data = await response.json()
      setServices(data)

    } catch (err) {
      setError(err.message || 'Unable to load services.')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    loadServices()
  }, [loadServices])

  const handleChange = (event) => {
    const { name, value } = event.target

    setForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  const resetForm = () => {
    setForm(emptyForm)
    setEditingId(null)
    setShowForm(false)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    setSaving(true)
    setError('')
    setMessage('')

    const requestBody = {
      serviceName: form.serviceName.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      durationMinutes: Number(form.durationMinutes)
    }

    try {
      const isEditing = editingId !== null

      const response = await fetch(
        isEditing
          ? `${API_URL}/api/services/${editingId}`
          : `${API_URL}/api/services`,
        {
          method: isEditing ? 'PUT' : 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(requestBody)
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
          data.message || `Unable to save service. HTTP ${response.status}`
        )
      }

      setMessage(
        isEditing
          ? 'Service updated successfully.'
          : 'Service added successfully.'
      )

      resetForm()
      await loadServices()

    } catch (err) {
      setError(err.message || 'Unable to save service.')
    } finally {
      setSaving(false)
    }
  }

  const startEditing = (service) => {
    setEditingId(service.id)

    setForm({
      serviceName: service.serviceName,
      description: service.description || '',
      price: String(service.price),
      durationMinutes: String(service.durationMinutes)
    })

    setShowForm(true)
    setError('')
    setMessage('')
  }

  const deactivateService = async (id) => {
    const confirmed = window.confirm(
      'Are you sure you want to deactivate this service?'
    )

    if (!confirmed) return

    setError('')
    setMessage('')

    try {
      const response = await fetch(
        `${API_URL}/api/services/${id}/deactivate`,
        {
          method: 'PATCH',
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        throw new Error(`Unable to deactivate service. HTTP ${response.status}`)
      }

      setMessage('Service deactivated successfully.')
      await loadServices()

    } catch (err) {
      setError(err.message || 'Unable to deactivate service.')
    }
  }

  return (
    <section className="services-page">

      <div className="section-heading">
        <div>
          <h2>Services</h2>
          <p>Manage the services offered by your business</p>
        </div>

        <button onClick={() => {
          if (showForm) {
            resetForm()
          } else {
            setShowForm(true)
            setError('')
            setMessage('')
          }
        }}>
          {showForm ? 'Close Form' : '+ Add Service'}
        </button>
      </div>

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      {showForm && (
        <form className="service-form" onSubmit={handleSubmit}>

          <h3>{editingId ? 'Edit Service' : 'Add New Service'}</h3>

          <label>Service Name</label>
          <input
            type="text"
            name="serviceName"
            placeholder="Example: Haircut"
            value={form.serviceName}
            onChange={handleChange}
            required
          />

          <label>Description</label>
          <textarea
            name="description"
            placeholder="Describe your service"
            value={form.description}
            onChange={handleChange}
            rows="3"
          />

          <label>Price (₹)</label>
          <input
            type="number"
            name="price"
            min="0"
            step="0.01"
            placeholder="Enter price"
            value={form.price}
            onChange={handleChange}
            required
          />

          <label>Duration (minutes)</label>
          <input
            type="number"
            name="durationMinutes"
            min="1"
            placeholder="Example: 30"
            value={form.durationMinutes}
            onChange={handleChange}
            required
          />

          <button type="submit" disabled={saving}>
            {saving
              ? 'Saving...'
              : editingId
                ? 'Update Service'
                : 'Save Service'}
          </button>

        </form>
      )}

      {loading ? (
        <p>Loading services...</p>
      ) : services.length === 0 ? (
        <div className="empty-state">
          <h3>No services found</h3>
          <p>Add your first business service.</p>
        </div>
      ) : (

        <div className="service-list">

          {services.map(service => (
            <div className="service-card" key={service.id}>

              <div className="service-info">
                <h3>{service.serviceName}</h3>

                <p>
                  {service.description || 'No description provided'}
                </p>

                <strong>₹{service.price}</strong>

                <p>Duration: {service.durationMinutes} minutes</p>

                <span className={
                  service.active
                    ? 'service-active'
                    : 'service-inactive'
                }>
                  {service.active ? 'ACTIVE' : 'INACTIVE'}
                </span>
              </div>

              <div className="service-actions">

                <button
                  onClick={() => startEditing(service)}
                >
                  Edit
                </button>

                {service.active && (
                  <button
                    className="service-deactivate"
                    onClick={() => deactivateService(service.id)}
                  >
                    Deactivate
                  </button>
                )}

              </div>

            </div>
          ))}

        </div>
      )}

    </section>
  )
}

export default Services
