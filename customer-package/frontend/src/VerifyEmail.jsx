import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function VerifyEmail() {
  const params = new URLSearchParams(window.location.search)
  const token = params.get('token')
  const isEmailChange = params.get('type') === 'email-change'

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const verify = async () => {
      if (!token) {
        setError('The verification link is invalid or missing.')
        setLoading(false)
        return
      }

      try {
        const endpoint = isEmailChange
          ? '/api/auth/verify-email-change'
          : '/api/auth/verify-email'

        const response = await fetch(`${API_URL}${endpoint}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ token })
        })

        const data = await response.json()

        if (!response.ok) {
          throw new Error(data.message || 'Email verification failed.')
        }

        setMessage(data.message || 'Email verified successfully.')

      } catch (err) {
        setError(err.message || 'Unable to verify email.')
      } finally {
        setLoading(false)
      }
    }

    verify()
  }, [token, isEmailChange])

  return (
    <div className="login-page">
      <div className="login-card">

        <h1 className="login-logo">SmartBook</h1>

        <h2>Email Verification</h2>

        {loading && <p>Verifying your email address...</p>}

        {message && (
          <p className="success-message">{message}</p>
        )}

        {error && (
          <p className="login-error">{error}</p>
        )}

        {!loading && (
          <p className="register-footer">
            <a href="/">Back to Sign In</a>
          </p>
        )}

      </div>
    </div>
  )
}

export default VerifyEmail