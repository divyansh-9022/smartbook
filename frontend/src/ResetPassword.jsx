import { useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080' : '')

function ResetPassword() {
  const token = new URLSearchParams(window.location.search).get('token')

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const handleSubmit = async (event) => {
    event.preventDefault()

    setError('')
    setMessage('')

    if (!token) {
      setError('The password reset link is invalid or missing.')
      return
    }

    if (newPassword.length < 8 || newPassword.length > 72) {
      setError('Password must contain between 8 and 72 characters.')
      return
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)

    try {
      const response = await fetch(`${API_URL}/api/auth/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          token,
          newPassword
        })
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data.message || 'Unable to reset password.')
      }

      setMessage('Your password has been reset successfully.')
      setNewPassword('')
      setConfirmPassword('')

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

        <h2>Reset Your Password</h2>

        <p className="login-subtitle">
          Choose a new password for your account.
        </p>

        <form onSubmit={handleSubmit}>

          <label>New Password</label>

          <input
            type="password"
            placeholder="Enter new password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            minLength={8}
            maxLength={72}
            required
          />

          <label>Confirm New Password</label>

          <input
            type="password"
            placeholder="Confirm new password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            minLength={8}
            maxLength={72}
            required
          />

          {error && (
            <p className="login-error">{error}</p>
          )}

          {message && (
            <p className="success-message">{message}</p>
          )}

          <button type="submit" disabled={loading || !token}>
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>

        </form>

        <p className="register-footer">
          <a href="/">Back to Sign In</a>
        </p>

        <p className="login-footer">Powered by SmartBook</p>

      </div>
    </div>
  )
}

export default ResetPassword