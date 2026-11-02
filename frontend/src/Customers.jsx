
import { useCallback, useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function Customers({ token }) {
  const [customers, setCustomers] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving] = useState(false)

  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [history, setHistory] = useState([])
  const [historyLoading, setHistoryLoading] = useState(false)

  const [form, setForm] = useState({
    customerName: '',
    customerEmail: '',
    customerPhone: ''
  })

  const loadCustomers = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const response = await fetch(`${API_URL}/api/customers`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      if (!response.ok) {
        throw new Error(`Unable to load customers. HTTP ${response.status}`)
      }

      setCustomers(await response.json())

    } catch (err) {
      setError(err.message || 'Unable to load customers.')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    loadCustomers()
  }, [loadCustomers])

  const handleInputChange = (event) => {
    const { name, value } = event.target

    setForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  const addCustomer = async (event) => {
    event.preventDefault()

    setSaving(true)
    setError('')
    setMessage('')

    try {
      const response = await fetch(`${API_URL}/api/customers`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(form)
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
          data.message || `Customer registration failed. HTTP ${response.status}`
        )
      }

      setForm({
        customerName: '',
        customerEmail: '',
        customerPhone: ''
      })

      setShowForm(false)
      setMessage('Customer added successfully.')

      await loadCustomers()

    } catch (err) {
      setError(err.message || 'Unable to add customer.')
    } finally {
      setSaving(false)
    }
  }

  const viewHistory = async (customer) => {
    if (selectedCustomer?.id === customer.id) {
      setSelectedCustomer(null)
      setHistory([])
      return
    }

    setSelectedCustomer(customer)
    setHistory([])
    setHistoryLoading(true)
    setError('')

    try {
      const response = await fetch(
        `${API_URL}/api/appointments/customer/${encodeURIComponent(customer.customerEmail)}`,
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        throw new Error(`Unable to load appointment history. HTTP ${response.status}`)
      }

      setHistory(await response.json())

    } catch (err) {
      setError(err.message || 'Unable to load appointment history.')
    } finally {
      setHistoryLoading(false)
    }
  }

  const filteredCustomers = customers.filter(customer => {
    const searchText = search.toLowerCase()

    return (
      customer.customerName.toLowerCase().includes(searchText) ||
      customer.customerEmail.toLowerCase().includes(searchText) ||
      (customer.customerPhone || '').includes(searchText)
    )
  })

  return (
    <section className="customers-page">

      <div className="section-heading">
        <div>
          <h2>Customers</h2>
          <p>Manage your customer records</p>
        </div>

        <button onClick={() => {
          setShowForm(!showForm)
          setError('')
          setMessage('')
        }}>
          {showForm ? 'Close Form' : '+ Add Customer'}
        </button>
      </div>

      {message && (
        <p className="success-message">{message}</p>
      )}

      {error && (
        <p className="login-error">{error}</p>
      )}

      {showForm && (
        <form className="customer-form" onSubmit={addCustomer}>

          <h3>Add New Customer</h3>

          <label>Customer Name</label>
          <input
            type="text"
            name="customerName"
            value={form.customerName}
            onChange={handleInputChange}
            required
          />

          <label>Email Address</label>
          <input
            type="email"
            name="customerEmail"
            value={form.customerEmail}
            onChange={handleInputChange}
            required
          />

          <label>Phone Number</label>
          <input
            type="tel"
            name="customerPhone"
            value={form.customerPhone}
            onChange={handleInputChange}
          />

          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save Customer'}
          </button>

        </form>
      )}

      <input
        type="search"
        className="customer-search"
        placeholder="Search by name, email or phone..."
        value={search}
        onChange={event => setSearch(event.target.value)}
      />

      {loading ? (
        <p>Loading customers...</p>
      ) : filteredCustomers.length === 0 ? (
        <div className="empty-state">
          <h3>No customers found</h3>
          <p>Add a customer or try another search.</p>
        </div>
      ) : (

        <div className="customer-list">

          {filteredCustomers.map(customer => (
            <div className="customer-card" key={customer.id}>

              <div className="customer-avatar">
                {customer.customerName.charAt(0).toUpperCase()}
              </div>

              <div className="customer-info">
                <h3>{customer.customerName}</h3>
                <p>{customer.customerEmail}</p>
                <p>{customer.customerPhone || 'No phone number provided'}</p>

                <small>
                  Customer since:{' '}
                  {new Date(customer.createdAt).toLocaleDateString()}
                </small>

                <button
                  className="history-button"
                  onClick={() => viewHistory(customer)}
                >
                  {selectedCustomer?.id === customer.id
                    ? 'Hide Appointment History'
                    : 'View Appointment History'}
                </button>
              </div>

              {selectedCustomer?.id === customer.id && (
                <div className="customer-history">

                  <h4>Appointment History</h4>

                  {historyLoading ? (
                    <p>Loading appointment history...</p>
                  ) : history.length === 0 ? (
                    <p>No appointments found for this customer.</p>
                  ) : (
                    history.map(appointment => (
                      <div
                        className="history-item"
                        key={appointment.id}
                      >
                        <strong>{appointment.serviceName}</strong>

                        <p>
                          {new Date(
                            appointment.appointmentDateTime
                          ).toLocaleString()}
                        </p>

                        <p>₹{appointment.price}</p>

                        <span>{appointment.status}</span>
                      </div>
                    ))
                  )}

                </div>
              )}

            </div>
          ))}

        </div>
      )}

    </section>
  )
}

export default Customers
