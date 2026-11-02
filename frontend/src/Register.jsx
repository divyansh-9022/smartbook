
import { useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function Register({ onRegistered, onShowLogin }) {
  const [form, setForm] = useState({
    businessName: '',
    ownerName: '',
    email: '',
    password: '',
    phoneNumber: ''
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [showVerificationPopup, setShowVerificationPopup] = useState(false)
  const [registeredEmail, setRegisteredEmail] = useState('')

  const handleChange = (event) => {
    const { name, value } = event.target

    setForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    setLoading(true)
    setError('')
    setMessage('')

    try {
      const response = await fetch(
        `${API_URL}/api/business-owners/register`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(form)
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
          data.message ||
          `Registration failed. HTTP ${response.status}`
        )
      }

      setRegisteredEmail(data.email || form.email)
      setShowVerificationPopup(true)

      setForm({
        businessName: '',
        ownerName: '',
        email: '',
        password: '',
        phoneNumber: ''
      })

    } catch (err) {
      setError(err.message || 'Unable to register your business.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="register-page">
      <div className="register-card">

        <h1 className="register-logo">SmartBook</h1>

        <h2>Create Your Business Account</h2>

        <p className="register-subtitle">
          Start managing your appointments with SmartBook
        </p>

        {message && (
          <p className="success-message">{message}</p>
        )}

        {error && (
          <p className="login-error">{error}</p>
        )}

        <form onSubmit={handleSubmit}>

          <label>Business Name</label>
          <input
            type="text"
            name="businessName"
            placeholder="Enter your business name"
            value={form.businessName}
            onChange={handleChange}
            required
          />

          <label>Owner Name</label>
          <input
            type="text"
            name="ownerName"
            placeholder="Enter your full name"
            value={form.ownerName}
            onChange={handleChange}
            required
          />

          <label>Email Address</label>
          <input
            type="email"
            name="email"
            placeholder="Enter your email"
            value={form.email}
            onChange={handleChange}
            required
          />

          <label>Password</label>
          <input
            type="password"
            name="password"
            placeholder="Create a password (8–72 characters)"
            value={form.password}
            onChange={handleChange}
            minLength={8}
            maxLength={72}
            required
          />

          <label>Phone Number</label>
          <input
            type="tel"
            name="phoneNumber"
            placeholder="Enter your phone number"
            value={form.phoneNumber}
            onChange={handleChange}
          />

          <button type="submit" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>

        </form>

        <p className="register-footer">
          Already have an account?{' '}
          <button
            type="button"
            className="text-button"
            onClick={onShowLogin}
          >
            Sign In
          </button>
        </p>
        {showVerificationPopup && (
          <div className="verification-overlay">
            <div className="verification-popup">

              <div className="verification-icon">✉️</div>

              <h2>Verify Your Email Address</h2>

              <p>
                Your SmartBook business account has been created successfully!
              </p>

              <p>
                We've sent a verification link to:
              </p>

              <strong>{registeredEmail}</strong>

              <p>
                Please open your inbox and click the verification link
                to activate your account.
              </p>

              <p className="verification-note">
                Check your Spam or Promotions folder if you cannot find the email.
              </p>

              <button
                type="button"
                onClick={onRegistered}
              >
                Back to Sign In
              </button>

            </div>
          </div>
        )}

      </div>
    </div>
  )
}

export default Register
