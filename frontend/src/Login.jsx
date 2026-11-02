
import { useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function Login({ onLogin, onShowRegister }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showForgotPassword, setShowForgotPassword] = useState(false)
  const [resetMessage, setResetMessage] = useState('')

  const handleLogin = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
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
          data.message || `Login failed. HTTP ${response.status}`
        )
      }

      if (!data.token) {
        throw new Error('Login token was not received.')
      }

      sessionStorage.setItem('smartbookToken', data.token)
      sessionStorage.setItem(
        'smartbookTokenType',
        data.tokenType || 'Bearer'
      )

      onLogin(data.token)

    } catch (err) {
      setError(err.message || 'Unable to connect to SmartBook.')
    } finally {
      setLoading(false)
    }
  }
const handleForgotPassword = async (event) => {
  event.preventDefault()

  setError('')
  setResetMessage('')
  setLoading(true)

  try {
    const response = await fetch(`${API_URL}/api/auth/forgot-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email })
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message || 'Unable to request password reset.')
    }

    setResetMessage(data.message)

  } catch (err) {
    setError(err.message || 'Unable to connect to SmartBook.')
  } finally {
    setLoading(false)
  }
}

  return (
    <div className="login-page">
      <div className="login-card">

        <h1 className="login-logo">SmartBook</h1>

        <h2>Welcome Back!</h2>

        <p className="login-subtitle">
          Sign in to manage your business
        </p>

        <form onSubmit={showForgotPassword ? handleForgotPassword : handleLogin}>

          <label>Email Address</label>
          <input
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          {!showForgotPassword && (
            <>
              <label>Password</label>

              <input
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
              />

              <button
                type="button"
                className="text-button"
                onClick={() => {
                  setShowForgotPassword(true)
                  setError('')
                  setResetMessage('')
                }}
              >
                Forgot Password?
              </button>
            </>
          )}

          {showForgotPassword && (
            <p>Enter your email address to receive a password reset link.</p>
          )}

          {error && (
            <p className="login-error">{error}</p>
          )}

          {resetMessage && (
            <p className="success-message">{resetMessage}</p>
          )}

          <button type="submit" disabled={loading}>
            {loading
              ? (showForgotPassword ? 'Sending...' : 'Signing in...')
              : (showForgotPassword ? 'Send Reset Link' : 'Sign In')}
          </button>

          {showForgotPassword && (
            <button
              type="button"
              className="text-button"
              onClick={() => {
                setShowForgotPassword(false)
                setError('')
                setResetMessage('')
              }}
            >
              Back to Sign In
            </button>
          )}

        </form>

        <p className="register-footer">
          Don't have an account?{' '}
          <button
            type="button"
            className="text-button"
            onClick={onShowRegister}
          >
            Create Account
          </button>
        </p>

        <p className="login-footer">Powered by SmartBook</p>

      </div>
    </div>
  )
}

export default Login
